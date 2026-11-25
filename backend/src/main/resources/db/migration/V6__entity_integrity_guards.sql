-- Database guards supplement service authorization and transactional state transitions.
-- No migration is run by this development task against the application's database.
CREATE FUNCTION reject_append_only_mutation() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION '% is append-only; append a compensating event', TG_TABLE_NAME; END $$;
CREATE TRIGGER immutable_audit BEFORE UPDATE OR DELETE ON audit_logs FOR EACH ROW EXECUTE FUNCTION reject_append_only_mutation();
CREATE TRIGGER immutable_decision BEFORE UPDATE OR DELETE ON approval_decisions FOR EACH ROW EXECUTE FUNCTION reject_append_only_mutation();
CREATE TRIGGER immutable_leave_transaction BEFORE UPDATE OR DELETE ON leave_balance_transactions FOR EACH ROW EXECUTE FUNCTION reject_append_only_mutation();
CREATE TRIGGER immutable_attendance_adjustment BEFORE UPDATE OR DELETE ON attendance_adjustments FOR EACH ROW EXECUTE FUNCTION reject_append_only_mutation();

CREATE FUNCTION protect_closed_attendance() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF TG_OP<>'INSERT' AND EXISTS(SELECT 1 FROM attendance_closings WHERE id=OLD.closing_id AND status='CLOSED') THEN
   RAISE EXCEPTION 'Closed attendance requires authorized reopening before correction';
 END IF;
 IF TG_OP='DELETE' THEN RETURN OLD; END IF;
 IF TG_OP='UPDATE' AND ROW(NEW.check_in,NEW.check_out,NEW.schedule_assignment_id,NEW.overtime_minutes)
    IS DISTINCT FROM ROW(OLD.check_in,OLD.check_out,OLD.schedule_assignment_id,OLD.overtime_minutes) THEN
   NEW.approved_overtime_minutes:=0; NEW.overtime_approved_by_id:=NULL;
   NEW.overtime_approved_at:=NULL; NEW.overtime_approval_reason:=NULL;
 END IF;
 IF EXISTS(SELECT 1 FROM attendance_closings WHERE id=NEW.closing_id AND status='CLOSED')
    AND (NEW.calculation_snapshot IS NULL OR NEW.calculation_version IS NULL) THEN
   RAISE EXCEPTION 'Closing attendance requires its calculation snapshot and version';
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER closed_attendance BEFORE INSERT OR UPDATE OR DELETE ON attendances FOR EACH ROW EXECUTE FUNCTION protect_closed_attendance();

CREATE FUNCTION prevent_self_approval() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM approval_tasks t JOIN workflow_requests w ON w.id=t.request_id
     JOIN user_accounts a ON a.id=NEW.actor_id
     WHERE t.id=NEW.task_id AND (w.submitted_by_id=a.id OR w.target_employee_id=a.employee_id)) THEN
   RAISE EXCEPTION 'An account cannot approve its own request or a request about itself';
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER approval_not_self BEFORE INSERT ON approval_decisions FOR EACH ROW EXECUTE FUNCTION prevent_self_approval();

CREATE FUNCTION protect_legacy_provenance() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF TG_OP='INSERT' THEN
   IF TG_TABLE_NAME='user_accounts' THEN
     IF NEW.legacy_role IS NOT NULL THEN RAISE EXCEPTION 'New accounts cannot write archived legacy_role'; END IF;
   ELSIF NEW.legacy_leave_type IS NOT NULL OR NEW.legacy_status IS NOT NULL OR NEW.legacy_requester_id IS NOT NULL
      OR NEW.legacy_approver_id IS NOT NULL OR NEW.legacy_decision_note IS NOT NULL OR NEW.legacy_decided_at IS NOT NULL THEN
     RAISE EXCEPTION 'New leave requests cannot forge imported legacy provenance';
   END IF;
   RETURN NEW;
 END IF;
 IF TG_TABLE_NAME='user_accounts' THEN
   IF NEW.legacy_role IS DISTINCT FROM OLD.legacy_role THEN RAISE EXCEPTION 'legacy_role is archived'; END IF;
 ELSE
   IF ROW(NEW.legacy_leave_type,NEW.legacy_status,NEW.legacy_requester_id,NEW.legacy_approver_id,NEW.legacy_decision_note,NEW.legacy_decided_at)
      IS DISTINCT FROM ROW(OLD.legacy_leave_type,OLD.legacy_status,OLD.legacy_requester_id,OLD.legacy_approver_id,OLD.legacy_decision_note,OLD.legacy_decided_at)
   THEN RAISE EXCEPTION 'Legacy leave provenance is archived'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER archived_account_role BEFORE INSERT OR UPDATE ON user_accounts FOR EACH ROW EXECUTE FUNCTION protect_legacy_provenance();
CREATE TRIGGER archived_leave BEFORE INSERT OR UPDATE ON leave_requests FOR EACH ROW EXECUTE FUNCTION protect_legacy_provenance();

-- Ledger is the only writer of entitlement totals. Direct balance edits are rejected.
ALTER TABLE leave_entitlements ALTER COLUMN entitled_units SET DEFAULT 0;
ALTER TABLE leave_entitlements ALTER COLUMN carried_units SET DEFAULT 0;
ALTER TABLE leave_entitlements ALTER COLUMN adjusted_units SET DEFAULT 0;
ALTER TABLE leave_entitlements ALTER COLUMN used_units SET DEFAULT 0;
ALTER TABLE leave_entitlements ALTER COLUMN reserved_units SET DEFAULT 0;
CREATE FUNCTION guard_entitlement_totals() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF TG_OP='INSERT' THEN
   IF ROW(NEW.entitled_units,NEW.carried_units,NEW.adjusted_units,NEW.used_units,NEW.reserved_units)
      IS DISTINCT FROM ROW(0::numeric,0::numeric,0::numeric,0::numeric,0::numeric)
   THEN RAISE EXCEPTION 'Initial balance requires ledger entries'; END IF;
 ELSIF pg_trigger_depth() < 2 AND
   ROW(NEW.entitled_units,NEW.carried_units,NEW.adjusted_units,NEW.used_units,NEW.reserved_units)
   IS DISTINCT FROM ROW(OLD.entitled_units,OLD.carried_units,OLD.adjusted_units,OLD.used_units,OLD.reserved_units)
 THEN RAISE EXCEPTION 'Balance totals are ledger projections'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER entitlement_totals BEFORE INSERT OR UPDATE ON leave_entitlements FOR EACH ROW EXECUTE FUNCTION guard_entitlement_totals();
CREATE FUNCTION apply_leave_ledger() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 UPDATE leave_entitlements SET entitled_units=entitled_units+NEW.entitled_delta,
 carried_units=carried_units+NEW.carried_delta, adjusted_units=adjusted_units+NEW.adjusted_delta,
 used_units=used_units+NEW.used_delta, reserved_units=reserved_units+NEW.reserved_delta,
 version=version+1, updated_at=LOCALTIMESTAMP, updated_by=NEW.created_by WHERE id=NEW.entitlement_id;
 RETURN NEW;
END $$;
CREATE TRIGGER apply_leave_balance AFTER INSERT ON leave_balance_transactions FOR EACH ROW EXECUTE FUNCTION apply_leave_ledger();

-- All workflow parents and children must be committed together, with one correct subtype.
CREATE FUNCTION validate_workflow_identity(request_id BIGINT) RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE w workflow_requests; subtype_count INTEGER; legacy BOOLEAN; l leave_requests;
BEGIN
 SELECT * INTO w FROM workflow_requests WHERE id=request_id;
 IF NOT FOUND THEN RETURN; END IF;
 SELECT (SELECT count(*) FROM leave_requests WHERE workflow_request_id=w.id)
      + (SELECT count(*) FROM transfer_requests WHERE workflow_request_id=w.id)
      + (SELECT count(*) FROM appointment_requests WHERE workflow_request_id=w.id)
      + (SELECT count(*) FROM resignation_requests WHERE workflow_request_id=w.id) INTO subtype_count;
 IF subtype_count<>1 OR
   (w.request_type='LEAVE' AND NOT EXISTS(SELECT 1 FROM leave_requests WHERE workflow_request_id=w.id)) OR
   (w.request_type='TRANSFER' AND NOT EXISTS(SELECT 1 FROM transfer_requests WHERE workflow_request_id=w.id)) OR
   (w.request_type='APPOINTMENT' AND NOT EXISTS(SELECT 1 FROM appointment_requests WHERE workflow_request_id=w.id)) OR
   (w.request_type='RESIGNATION' AND NOT EXISTS(SELECT 1 FROM resignation_requests WHERE workflow_request_id=w.id))
 THEN RAISE EXCEPTION 'Workflow % requires exactly one matching subtype',w.id; END IF;
 SELECT * INTO l FROM leave_requests WHERE workflow_request_id=w.id;
 legacy := FOUND AND l.legacy_status IS NOT NULL;
 IF w.status NOT IN ('DRAFT','CANCELLED') AND NOT legacy AND
   (w.submitted_by_id IS NULL OR w.submitted_at IS NULL OR w.routing_snapshot IS NULL)
 THEN RAISE EXCEPTION 'Submitted workflow needs account, time and routing snapshot'; END IF;
 IF w.request_type='LEAVE' AND w.status NOT IN ('DRAFT','CANCELLED') AND NOT legacy AND
   (l.start_date IS NULL OR l.end_date IS NULL OR l.leave_type_id IS NULL OR l.reason IS NULL OR l.total_units IS NULL OR l.policy_snapshot IS NULL)
 THEN RAISE EXCEPTION 'Submitted leave is incomplete'; END IF;
 IF EXISTS(SELECT 1 FROM transfer_requests t LEFT JOIN employee_assignments a ON a.id=t.from_assignment_id
    WHERE t.workflow_request_id=w.id AND ((a.employee_id IS NOT NULL AND a.employee_id<>w.target_employee_id)
    OR (w.status NOT IN ('DRAFT','CANCELLED') AND (t.from_assignment_id IS NULL OR t.to_department_id IS NULL OR t.to_position_id IS NULL OR t.proposed_effective_date IS NULL OR t.reason IS NULL))))
 THEN RAISE EXCEPTION 'Invalid transfer source or submitted fields'; END IF;
 IF EXISTS(SELECT 1 FROM appointment_requests t LEFT JOIN employee_assignments a ON a.id=t.from_assignment_id
    WHERE t.workflow_request_id=w.id AND ((a.employee_id IS NOT NULL AND a.employee_id<>w.target_employee_id)
    OR (w.status NOT IN ('DRAFT','CANCELLED') AND (t.from_assignment_id IS NULL OR t.to_department_id IS NULL OR t.to_position_id IS NULL OR t.proposed_effective_date IS NULL OR t.reason IS NULL))))
 THEN RAISE EXCEPTION 'Invalid appointment source or submitted fields'; END IF;
 IF EXISTS(SELECT 1 FROM resignation_requests r WHERE r.workflow_request_id=w.id AND
    ((w.status NOT IN ('DRAFT','CANCELLED') AND (r.proposed_last_working_date IS NULL OR r.reason IS NULL)) OR
     (w.status IN ('APPROVED','COMPLETED') AND (r.approved_last_working_date IS NULL OR r.effective_date IS NULL))))
 THEN RAISE EXCEPTION 'Resignation dates and reason required by state'; END IF;
END $$;
CREATE FUNCTION check_workflow_identity() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF TG_TABLE_NAME='workflow_requests' THEN
   IF TG_OP<>'DELETE' THEN PERFORM validate_workflow_identity(NEW.id); END IF;
 ELSE
   IF TG_OP<>'INSERT' THEN PERFORM validate_workflow_identity(OLD.workflow_request_id); END IF;
   IF TG_OP<>'DELETE' THEN PERFORM validate_workflow_identity(NEW.workflow_request_id); END IF;
 END IF;
 RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER workflow_identity AFTER INSERT OR UPDATE ON workflow_requests DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_workflow_identity();
CREATE CONSTRAINT TRIGGER leave_identity AFTER INSERT OR UPDATE OR DELETE ON leave_requests DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_workflow_identity();
CREATE CONSTRAINT TRIGGER transfer_identity AFTER INSERT OR UPDATE OR DELETE ON transfer_requests DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_workflow_identity();
CREATE CONSTRAINT TRIGGER appointment_identity AFTER INSERT OR UPDATE OR DELETE ON appointment_requests DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_workflow_identity();
CREATE CONSTRAINT TRIGGER resignation_identity AFTER INSERT OR UPDATE OR DELETE ON resignation_requests DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_workflow_identity();
CREATE UNIQUE INDEX uk_resignation_processing ON workflow_requests(target_employee_id) WHERE request_type='RESIGNATION' AND status IN ('PENDING','APPROVED');

-- A payslip cannot point at a different employee/run's input.
-- Seven-entity payroll: calculation state belongs to the period, payment to the slip.
ALTER TABLE payroll_periods
 ADD CONSTRAINT ck_payroll_periods_totals CHECK (total_net=total_gross-total_deductions),
 ADD CONSTRAINT ck_payroll_periods_calculated CHECK (status NOT IN ('CALCULATED','REVIEWED','APPROVED','CLOSED') OR (calculated_by_id IS NOT NULL AND calculated_at IS NOT NULL)),
 ADD CONSTRAINT ck_payroll_periods_reviewed CHECK (status NOT IN ('REVIEWED','APPROVED','CLOSED') OR (reviewed_by_id IS NOT NULL AND reviewed_at IS NOT NULL)),
 ADD CONSTRAINT ck_payroll_periods_approved CHECK (status NOT IN ('APPROVED','CLOSED') OR (approved_by_id IS NOT NULL AND approved_at IS NOT NULL)),
 ADD CONSTRAINT ck_payroll_periods_publication CHECK ((published_by_id IS NULL AND published_at IS NULL) OR (published_by_id IS NOT NULL AND published_at IS NOT NULL AND status IN ('APPROVED','CLOSED'))),
 ADD CONSTRAINT ck_payroll_periods_closed CHECK (status<>'CLOSED' OR (closed_by_id IS NOT NULL AND closed_at IS NOT NULL AND published_at IS NOT NULL));

ALTER TABLE payslips
 ADD CONSTRAINT ck_payslips_source_hash CHECK (source_hash ~ '^[0-9a-f]{64}$'),
 ADD CONSTRAINT ck_payslips_snapshot CHECK (COALESCE(
   jsonb_typeof(input_snapshot)='object' AND input_snapshot @> '{"schemaVersion":1}'::jsonb
   AND input_snapshot ?& ARRAY['standardUnits','payableUnits','attendedUnits','approvedOvertimeMinutes','employeeProfile','sourceData']
   AND jsonb_typeof(input_snapshot->'employeeProfile')='object'
   AND input_snapshot->'employeeProfile' ?& ARRAY['taxCode','insuranceNumber','insuranceBaseSalary','taxResidencyType','dependentCount','bankName','bankAccountNumber','bankAccountHolder','currency']
   AND input_snapshot->'employeeProfile'->>'currency'=currency
   AND (input_snapshot->'employeeProfile'->>'dependentCount')::integer>=0
   AND jsonb_typeof(input_snapshot->'sourceData')='object'
   AND input_snapshot->'sourceData' @> '{"schemaVersion":1}'::jsonb
   AND input_snapshot->'sourceData' ?& ARRAY['policies','compensations','contracts','schedules','assignments','components','attendance','leavePolicies','leaveDays','lockedReviews','adjustments','roundingRule','calculationEngineVersion']
   AND jsonb_typeof(input_snapshot->'standardUnits')='number'
   AND jsonb_typeof(input_snapshot->'payableUnits')='number'
   AND jsonb_typeof(input_snapshot->'attendedUnits')='number'
   AND jsonb_typeof(input_snapshot->'approvedOvertimeMinutes')='number'
   AND (input_snapshot->>'standardUnits')::numeric>=0
   AND (input_snapshot->>'payableUnits')::numeric>=0
   AND (input_snapshot->>'attendedUnits')::numeric>=0
   AND (input_snapshot->>'approvedOvertimeMinutes')::integer>=0
   AND (input_type<>'REGULAR' OR attendance_closing_id IS NOT NULL)
   AND (input_type<>'ADJUSTMENT_ONLY' OR ((input_snapshot->>'standardUnits')::numeric=0 AND (input_snapshot->>'payableUnits')::numeric=0
     AND (input_snapshot->>'attendedUnits')::numeric=0 AND (input_snapshot->>'approvedOvertimeMinutes')::integer=0)), false)),
 ADD CONSTRAINT ck_payslips_payment CHECK (
   (payment_status='UNPAID' AND paid_at IS NULL AND paid_by_id IS NULL AND payment_method IS NULL
     AND payment_reference IS NULL AND payment_evidence_reference IS NULL AND payment_idempotency_key IS NULL)
   OR (payment_status='PAID' AND paid_at IS NOT NULL AND paid_by_id IS NOT NULL AND payment_method IS NOT NULL
     AND net_pay>0 AND NULLIF(BTRIM(payment_idempotency_key),'') IS NOT NULL
     AND (NULLIF(BTRIM(payment_reference),'') IS NOT NULL OR NULLIF(BTRIM(payment_evidence_reference),'') IS NOT NULL)));

ALTER TABLE payroll_items
 ADD CONSTRAINT ck_payroll_items_calculation_detail_schema CHECK (COALESCE(jsonb_typeof(calculation_detail)='object' AND calculation_detail @> '{"schemaVersion":1}'::jsonb,false)),
 ADD CONSTRAINT ck_payroll_items_adjustment CHECK (
   (origin='ADJUSTMENT' AND original_payslip_id IS NOT NULL AND NULLIF(BTRIM(adjustment_code),'') IS NOT NULL AND NULLIF(BTRIM(reason),'') IS NOT NULL)
   OR (origin<>'ADJUSTMENT' AND original_payslip_id IS NULL AND original_item_id IS NULL AND adjustment_code IS NULL)),
 ADD CONSTRAINT ck_payroll_items_manual_reason CHECK (origin<>'MANUAL' OR NULLIF(BTRIM(reason),'') IS NOT NULL);

CREATE FUNCTION protect_payroll_period() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE totals RECORD;
BEGIN
 IF TG_OP='INSERT' THEN
   IF NEW.status<>'OPEN' THEN RAISE EXCEPTION 'Create payroll periods in OPEN state'; END IF;
   RETURN NEW;
 END IF;
 IF OLD.status='CLOSED' THEN RAISE EXCEPTION 'Closed payroll period is immutable'; END IF;
 IF TG_OP='DELETE' THEN
   IF OLD.status='APPROVED' THEN RAISE EXCEPTION 'Cannot delete approved payroll'; END IF;
   RETURN OLD;
 END IF;
 IF OLD.status='APPROVED' THEN
   IF NEW.status NOT IN ('APPROVED','CLOSED') OR
      (to_jsonb(NEW)-ARRAY['status','published_by_id','published_at','closed_by_id','closed_at','updated_at','updated_by','version'])
      IS DISTINCT FROM (to_jsonb(OLD)-ARRAY['status','published_by_id','published_at','closed_by_id','closed_at','updated_at','updated_by','version'])
   THEN RAISE EXCEPTION 'Approved calculation is immutable; adjust in a later period'; END IF;
   IF OLD.published_at IS NOT NULL AND (NEW.published_at IS DISTINCT FROM OLD.published_at OR NEW.published_by_id IS DISTINCT FROM OLD.published_by_id)
   THEN RAISE EXCEPTION 'Publication identity is immutable'; END IF;
   RETURN NEW;
 END IF;
 IF NEW.status<>OLD.status AND NOT (
   (OLD.status='OPEN' AND NEW.status='INPUT_LOCKED') OR
   (OLD.status='INPUT_LOCKED' AND NEW.status IN ('OPEN','CALCULATED')) OR
   (OLD.status='CALCULATED' AND NEW.status IN ('INPUT_LOCKED','REVIEWED')) OR
   (OLD.status='REVIEWED' AND NEW.status IN ('INPUT_LOCKED','APPROVED')))
 THEN RAISE EXCEPTION 'Invalid payroll period transition % -> %',OLD.status,NEW.status; END IF;
 IF (NEW.start_date,NEW.end_date,NEW.policy_id) IS DISTINCT FROM (OLD.start_date,OLD.end_date,OLD.policy_id)
    AND NEW.status NOT IN ('OPEN','INPUT_LOCKED')
 THEN RAISE EXCEPTION 'Reopen the draft before changing payroll inputs'; END IF;
 IF NEW.status IN ('OPEN','INPUT_LOCKED') THEN
   NEW.calculated_by_id:=NULL; NEW.calculated_at:=NULL;
   NEW.reviewed_by_id:=NULL; NEW.reviewed_at:=NULL; NEW.review_note:=NULL;
   NEW.approved_by_id:=NULL; NEW.approved_at:=NULL;
   NEW.total_gross:=0; NEW.total_deductions:=0; NEW.total_net:=0;
 END IF;
 IF NEW.status='CALCULATED' THEN
   NEW.reviewed_by_id:=NULL; NEW.reviewed_at:=NULL; NEW.review_note:=NULL;
   NEW.approved_by_id:=NULL; NEW.approved_at:=NULL;
 END IF;
 IF NEW.status IN ('CALCULATED','REVIEWED','APPROVED') THEN
   IF NOT EXISTS(SELECT 1 FROM payslips WHERE period_id=NEW.id) THEN RAISE EXCEPTION 'Payroll period needs payslips'; END IF;
   IF EXISTS(SELECT 1 FROM payslips p WHERE p.period_id=NEW.id AND
      (p.gross_pay<>COALESCE((SELECT sum(i.amount) FROM payroll_items i WHERE i.payslip_id=p.id AND i.component_type_snapshot='EARNING'),0)
       OR p.employee_deductions<>COALESCE((SELECT sum(i.amount) FROM payroll_items i WHERE i.payslip_id=p.id AND i.component_type_snapshot='DEDUCTION'),0)
       OR p.employer_contributions<>COALESCE((SELECT sum(i.amount) FROM payroll_items i WHERE i.payslip_id=p.id AND i.component_type_snapshot='EMPLOYER_CONTRIBUTION'),0)))
   THEN RAISE EXCEPTION 'Payslip totals must reconcile with payroll items'; END IF;
   SELECT sum(gross_pay) gross,sum(employee_deductions) deductions,sum(net_pay) net INTO totals FROM payslips WHERE period_id=NEW.id;
   IF NEW.total_gross<>totals.gross OR NEW.total_deductions<>totals.deductions OR NEW.total_net<>totals.net
   THEN RAISE EXCEPTION 'Period totals must reconcile with payslips'; END IF;
   IF EXISTS(SELECT 1 FROM payslips p WHERE p.period_id=NEW.id AND
      (p.net_pay<0 OR p.currency<>(SELECT currency FROM payroll_policies WHERE id=NEW.policy_id)
       OR (p.input_type='REGULAR' AND NOT EXISTS(SELECT 1 FROM attendance_closings c WHERE c.id=p.attendance_closing_id AND c.status='CLOSED' AND c.period_start<=NEW.start_date AND c.period_end>=NEW.end_date))
       OR (p.performance_review_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM performance_reviews r WHERE r.id=p.performance_review_id AND r.employee_id=p.employee_id AND r.status='LOCKED'))))
   THEN RAISE EXCEPTION 'Payroll requires matching currency and closed attendance / locked KPI sources'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER guarded_payroll_period BEFORE INSERT OR UPDATE OR DELETE ON payroll_periods FOR EACH ROW EXECUTE FUNCTION protect_payroll_period();

-- All child writers lock the parent period. Approval and edits therefore serialize.
CREATE FUNCTION invalidate_payroll_draft(period_value BIGINT) RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
 UPDATE payroll_periods SET status='INPUT_LOCKED',version=version+1
 WHERE id=period_value AND status IN ('CALCULATED','REVIEWED');
END $$;

CREATE FUNCTION protect_payslip() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE p payroll_periods; same_calculation BOOLEAN;
 payment_fields TEXT[]:=ARRAY['payment_status','paid_at','payment_method','payment_reference','payment_evidence_reference','payment_idempotency_key','paid_by_id'];
BEGIN
 SELECT * INTO p FROM payroll_periods WHERE id=CASE WHEN TG_OP='DELETE' THEN OLD.period_id ELSE NEW.period_id END FOR UPDATE;
 IF TG_OP='DELETE' THEN
   IF p.status IN ('APPROVED','CLOSED') THEN RAISE EXCEPTION 'Cannot delete approved payslip'; END IF;
   PERFORM invalidate_payroll_draft(p.id); RETURN OLD;
 END IF;
 IF TG_OP='INSERT' THEN
   IF p.status IN ('APPROVED','CLOSED') OR NEW.payment_status<>'UNPAID' THEN RAISE EXCEPTION 'Create unpaid payslips in a draft period'; END IF;
   PERFORM invalidate_payroll_draft(p.id); RETURN NEW;
 END IF;
 IF NEW.period_id<>OLD.period_id OR NEW.employee_id<>OLD.employee_id THEN RAISE EXCEPTION 'Payslip employee/period identity is immutable'; END IF;
 same_calculation:=(to_jsonb(NEW)-(payment_fields||ARRAY['updated_at','updated_by','version']))
   IS NOT DISTINCT FROM (to_jsonb(OLD)-(payment_fields||ARRAY['updated_at','updated_by','version']));
 IF p.status IN ('APPROVED','CLOSED') AND NOT same_calculation THEN RAISE EXCEPTION 'Approved payslip calculation is immutable'; END IF;
 IF OLD.payment_status='PAID' AND (to_jsonb(NEW)-(ARRAY['updated_at','updated_by','version']))
     IS DISTINCT FROM (to_jsonb(OLD)-(ARRAY['updated_at','updated_by','version']))
 THEN RAISE EXCEPTION 'A recorded full payment cannot be overwritten'; END IF;
 IF NEW.payment_status='PAID' AND p.status NOT IN ('APPROVED','CLOSED') THEN RAISE EXCEPTION 'Payment requires approved payroll'; END IF;
 IF NOT same_calculation THEN PERFORM invalidate_payroll_draft(p.id); END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER guarded_payslip BEFORE INSERT OR UPDATE OR DELETE ON payslips FOR EACH ROW EXECUTE FUNCTION protect_payslip();

CREATE FUNCTION protect_payroll_item() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE slip payslips; period_value payroll_periods; original payslips; original_period payroll_periods;
BEGIN
 SELECT * INTO slip FROM payslips WHERE id=CASE WHEN TG_OP='DELETE' THEN OLD.payslip_id ELSE NEW.payslip_id END;
 SELECT * INTO period_value FROM payroll_periods WHERE id=slip.period_id FOR UPDATE;
 IF period_value.status IN ('APPROVED','CLOSED') THEN RAISE EXCEPTION 'Approved payroll items are immutable'; END IF;
 IF TG_OP='DELETE' THEN PERFORM invalidate_payroll_draft(period_value.id); RETURN OLD; END IF;
 IF TG_OP='UPDATE' AND NEW.payslip_id<>OLD.payslip_id THEN RAISE EXCEPTION 'Cannot move a payroll item to another slip'; END IF;
 IF NEW.origin='ADJUSTMENT' THEN
   SELECT * INTO original FROM payslips WHERE id=NEW.original_payslip_id;
   SELECT * INTO original_period FROM payroll_periods WHERE id=original.period_id;
   IF original.employee_id IS DISTINCT FROM slip.employee_id OR original.currency IS DISTINCT FROM slip.currency
      OR original_period.status NOT IN ('APPROVED','CLOSED') OR original_period.end_date>=period_value.start_date
   THEN RAISE EXCEPTION 'Adjustment must refer to the same employee/currency in an earlier approved period'; END IF;
   IF NEW.original_item_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM payroll_items WHERE id=NEW.original_item_id AND payslip_id=original.id)
   THEN RAISE EXCEPTION 'Original item must belong to original payslip'; END IF;
 END IF;
 PERFORM invalidate_payroll_draft(period_value.id); RETURN NEW;
END $$;
CREATE TRIGGER guarded_payroll_item BEFORE INSERT OR UPDATE OR DELETE ON payroll_items FOR EACH ROW EXECUTE FUNCTION protect_payroll_item();

CREATE FUNCTION protect_locked_kpi() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE employee_value BIGINT; cycle_value BIGINT; assignment_value BIGINT;
BEGIN
 IF TG_TABLE_NAME='kpi_definitions' THEN
   IF EXISTS(SELECT 1 FROM employee_kpi_assignments a JOIN performance_reviews r
       ON r.cycle_id=a.cycle_id AND r.employee_id=a.employee_id
       WHERE a.definition_id=OLD.id AND r.status='LOCKED') THEN
     RAISE EXCEPTION 'KPI definition used by a locked review requires a new revision';
   END IF;
   IF TG_OP='DELETE' THEN RETURN OLD; END IF;
   RETURN NEW;
 END IF;
 IF TG_TABLE_NAME='performance_reviews' THEN
   IF TG_OP<>'INSERT' AND OLD.status='LOCKED' THEN RAISE EXCEPTION 'Locked performance review is immutable'; END IF;
   IF TG_OP='DELETE' THEN RETURN OLD; END IF;
   RETURN NEW;
 END IF;
 IF TG_TABLE_NAME='performance_cycles' THEN
   IF OLD.status='LOCKED' THEN RAISE EXCEPTION 'Locked performance cycle is immutable'; END IF;
   IF TG_OP='DELETE' THEN RETURN OLD; END IF;
   RETURN NEW;
 END IF;
 IF TG_TABLE_NAME='kpi_results' THEN
   assignment_value:=CASE WHEN TG_OP='DELETE' THEN OLD.assignment_id ELSE NEW.assignment_id END;
   SELECT employee_id,cycle_id INTO employee_value,cycle_value FROM employee_kpi_assignments WHERE id=assignment_value;
 ELSE
   employee_value:=CASE WHEN TG_OP='DELETE' THEN OLD.employee_id ELSE NEW.employee_id END;
   cycle_value:=CASE WHEN TG_OP='DELETE' THEN OLD.cycle_id ELSE NEW.cycle_id END;
 END IF;
 IF EXISTS(SELECT 1 FROM performance_reviews WHERE employee_id=employee_value AND cycle_id=cycle_value AND status='LOCKED')
 OR EXISTS(SELECT 1 FROM performance_cycles WHERE id=cycle_value AND status='LOCKED')
 THEN RAISE EXCEPTION 'KPI input belongs to a locked review/cycle'; END IF;
 IF TG_OP='UPDATE' THEN
   IF TG_TABLE_NAME='kpi_results' THEN
     SELECT employee_id,cycle_id INTO employee_value,cycle_value FROM employee_kpi_assignments WHERE id=OLD.assignment_id;
   ELSE employee_value:=OLD.employee_id; cycle_value:=OLD.cycle_id; END IF;
   IF EXISTS(SELECT 1 FROM performance_reviews WHERE employee_id=employee_value AND cycle_id=cycle_value AND status='LOCKED')
      OR EXISTS(SELECT 1 FROM performance_cycles WHERE id=cycle_value AND status='LOCKED')
   THEN RAISE EXCEPTION 'Cannot move a locked KPI input'; END IF;
 END IF;
 IF TG_OP='DELETE' THEN RETURN OLD; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER frozen_review BEFORE INSERT OR UPDATE OR DELETE ON performance_reviews FOR EACH ROW EXECUTE FUNCTION protect_locked_kpi();
CREATE TRIGGER frozen_cycle BEFORE UPDATE OR DELETE ON performance_cycles FOR EACH ROW EXECUTE FUNCTION protect_locked_kpi();
CREATE TRIGGER frozen_kpi_assignment BEFORE INSERT OR UPDATE OR DELETE ON employee_kpi_assignments FOR EACH ROW EXECUTE FUNCTION protect_locked_kpi();
CREATE TRIGGER frozen_kpi_result BEFORE INSERT OR UPDATE OR DELETE ON kpi_results FOR EACH ROW EXECUTE FUNCTION protect_locked_kpi();
CREATE TRIGGER frozen_kpi_definition BEFORE UPDATE OR DELETE ON kpi_definitions FOR EACH ROW EXECUTE FUNCTION protect_locked_kpi();
