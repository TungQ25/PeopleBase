-- Phase 1A integrity on additive fields only. Strict organization activation is HOLD.
-- PostgreSQL/Flyway transactional DDL. Do not use V7 alone as an activated schema.

ALTER TABLE organization_settings
    ADD CONSTRAINT fk_org_settings_created_by FOREIGN KEY (created_by_id)
        REFERENCES user_accounts(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_org_settings_approved_by FOREIGN KEY (approved_by_id)
        REFERENCES user_accounts(id) ON DELETE RESTRICT,
    ADD CONSTRAINT uk_org_settings_revision UNIQUE (singleton_key, revision),
    ADD CONSTRAINT ck_org_settings_singleton CHECK (singleton_key = 1),
    ADD CONSTRAINT ck_org_settings_revision CHECK (revision > 0),
    ADD CONSTRAINT ck_org_settings_version CHECK (version >= 0),
    ADD CONSTRAINT ck_org_settings_dates CHECK (effective_to IS NULL OR effective_to > effective_from),
    ADD CONSTRAINT ck_org_settings_timezone CHECK (btrim(business_timezone) <> ''),
    ADD CONSTRAINT ck_org_settings_domain CHECK (email_domain IS NULL OR btrim(email_domain) <> ''),
    ADD CONSTRAINT ck_org_settings_email_rule CHECK
        (email_naming_rule IS NULL OR jsonb_typeof(email_naming_rule) = 'object'),
    ADD CONSTRAINT ck_org_settings_code_format CHECK
        (employee_code_format IS NULL OR jsonb_typeof(employee_code_format) = 'object'),
    ADD CONSTRAINT ck_org_settings_approval_pair CHECK
        ((approved_by_id IS NULL) = (approved_at IS NULL)),
    ADD CONSTRAINT ck_org_settings_advance CHECK (max_advance_months IS NULL OR max_advance_months >= 0),
    ADD CONSTRAINT ck_org_settings_leave_hold CHECK (leave_registration_state = 'CONFIG'),
    ADD CONSTRAINT ck_org_settings_precision CHECK
        (leave_cutoff_precision IS NULL OR btrim(leave_cutoff_precision) <> ''),
    ADD CONSTRAINT ck_org_settings_advance_basis CHECK
        (leave_advance_basis IS NULL OR btrim(leave_advance_basis) <> ''),
    ADD CONSTRAINT ex_org_settings_dates EXCLUDE USING gist
        (singleton_key WITH =, daterange(effective_from, effective_to, '[)') WITH &&);

CREATE INDEX idx_org_settings_created_by ON organization_settings(created_by_id);
CREATE INDEX idx_org_settings_approved_by ON organization_settings(approved_by_id);

ALTER TABLE document_assets
    ADD CONSTRAINT fk_document_assets_created_by FOREIGN KEY (created_by_id)
        REFERENCES user_accounts(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_document_assets_uploaded_by FOREIGN KEY (uploaded_by_id)
        REFERENCES user_accounts(id) ON DELETE RESTRICT,
    ADD CONSTRAINT uk_document_assets_storage_key UNIQUE (storage_key),
    ADD CONSTRAINT ck_document_assets_key CHECK (btrim(storage_key) <> ''),
    ADD CONSTRAINT ck_document_assets_hash CHECK (sha256 ~ '^[0-9A-Fa-f]{64}$'),
    ADD CONSTRAINT ck_document_assets_size CHECK (byte_size >= 0),
    ADD CONSTRAINT ck_document_assets_content_type CHECK (btrim(content_type) <> ''),
    ADD CONSTRAINT ck_document_assets_classification CHECK (btrim(classification) <> ''),
    ADD CONSTRAINT ck_document_assets_version CHECK (version = 0);

CREATE INDEX idx_document_assets_created_by ON document_assets(created_by_id);
CREATE INDEX idx_document_assets_uploaded_by ON document_assets(uploaded_by_id);

ALTER TABLE departments ADD CONSTRAINT ck_departments_phase1a_hold CHECK (record_mode = 'LEGACY');
ALTER TABLE positions ADD CONSTRAINT ck_positions_phase1a_hold CHECK (record_mode = 'LEGACY');
ALTER TABLE employee_assignments
    ADD CONSTRAINT ck_emp_assignments_phase1a_hold CHECK (record_mode = 'LEGACY'),
    ADD CONSTRAINT ck_emp_assignments_management_level CHECK
        (management_level IS NULL OR btrim(management_level) <> ''),
    ADD CONSTRAINT ck_emp_assignments_reporting_profile CHECK
        (reporting_profile IS NULL OR btrim(reporting_profile) <> ''),
    ADD CONSTRAINT ck_emp_assignments_provenance CHECK
        (provenance IS NULL OR btrim(provenance) <> '');
ALTER TABLE manager_assignments
    ADD CONSTRAINT ck_mgr_assignments_phase1a_hold CHECK (record_mode = 'LEGACY'),
    ADD CONSTRAINT ck_mgr_assignments_management_level CHECK
        (management_level IS NULL OR btrim(management_level) <> '');

-- Dynamic timezone names are checked against PostgreSQL's installed tzdata at write time;
-- not against the session TimeZone and not in an index expression.
CREATE FUNCTION phase1a_settings_guard() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        IF OLD.approved_at IS NOT NULL THEN
            RAISE EXCEPTION USING ERRCODE = '23514', MESSAGE = 'Approved settings revision cannot be deleted';
        END IF;
        RETURN OLD;
    END IF;
    IF TG_OP = 'UPDATE' AND OLD.approved_at IS NOT NULL THEN
        -- Payload/approval are immutable. The sole lifecycle exception is closing an
        -- open interval once, preserving payload and revision identity. This enables
        -- a subsequent adjacent revision without rewriting historical config values.
        IF (to_jsonb(NEW) - 'effective_to') IS DISTINCT FROM (to_jsonb(OLD) - 'effective_to')
           OR (NEW.effective_to IS DISTINCT FROM OLD.effective_to
               AND NOT (OLD.effective_to IS NULL AND NEW.effective_to IS NOT NULL
                        AND NEW.effective_to > OLD.effective_from)) THEN
            RAISE EXCEPTION USING ERRCODE = '23514', MESSAGE = 'Approved settings payload is immutable; create a new revision';
        END IF;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_timezone_names WHERE name = NEW.business_timezone) THEN
        RAISE EXCEPTION USING ERRCODE = '23514', MESSAGE = 'Unknown explicit business timezone';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER phase1a_settings_guard BEFORE INSERT OR UPDATE OR DELETE ON organization_settings
    FOR EACH ROW EXECUTE FUNCTION phase1a_settings_guard();

-- Each asset ID denotes a fixed binary version, so corrections use a new asset ID/key.
-- Retention/purge and actual object immutability need a separately reviewed storage adapter.
CREATE FUNCTION phase1a_asset_guard() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'DELETE' OR NEW IS DISTINCT FROM OLD THEN
        RAISE EXCEPTION USING ERRCODE = '23514', MESSAGE = 'Asset metadata is immutable; create a new binary version';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER phase1a_asset_guard BEFORE UPDATE OR DELETE ON document_assets
    FOR EACH ROW EXECUTE FUNCTION phase1a_asset_guard();

-- HOLD: HR identity uniqueness/activation, CEO coverage, V1 hierarchy/vocabularies,
-- manager exclusions beyond legacy PRIMARY, READY Leave config and future source FKs.
-- No authorization, deactivation workflow, scheduler or organization engine in triggers.
