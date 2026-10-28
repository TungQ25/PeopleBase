package com.peoplebase.api.common.snapshot;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;

/**
 * JSONB value objects, schema 1. These are frozen inputs, never substitutes for
 * the owning entity's business foreign keys. No executable expressions are accepted.
 * Services must validate completeness for the operation before submit/lock.
 */
public final class Snapshots {
    private Snapshots() {}
    public record SourceReference(String entityType, Long id, Long version) {}
    public record FixedRule(int schemaVersion, Method method, BigDecimal amount,
                            BigDecimal rate, Integer scale, RoundingMode roundingMode,
                            Integer expiryMonth, Integer expiryDay) {
        public enum Method { MANUAL, ANNUAL_GRANT, CARRY_FORWARD, FIXED_MONTHLY,
            PRORATE_BY_PAYABLE_DAYS, PER_ATTENDED_DAY, PER_HOUR, ONE_TIME,
            PERFORMANCE_BASED, HIGHER_IS_BETTER, LOWER_IS_BETTER, QUALITATIVE, ROUND }
        public enum RoundingMode { UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN, UNNECESSARY }
    }
    public record ApprovalStep(int stepNo, String stepCode, Long permissionId,
                               Long assignedAccountId, String routingReason) {}
    public record RoutingSnapshot(int schemaVersion, List<ApprovalStep> steps,
                                  boolean migratedLegacy, Long legacyRequestId,
                                  Long legacyApproverEmployeeId, String legacyDecisionNote,
                                  LocalDateTime legacyDecidedAt, String provenance) {}
    public record ScheduleSnapshot(int schemaVersion, SourceReference assignment,
                                   SourceReference schedule, SourceReference calendarDay,
                                   LocalDate workDate, LocalTime startTime, LocalTime endTime,
                                   LocalTime breakStart, LocalTime breakEnd,
                                   int scheduledMinutes, boolean companyPaidHoliday) {}
    public record LeavePolicySnapshot(int schemaVersion, SourceReference policy,
                                      String leaveTypeCode, String unit, String paymentSource,
                                      BigDecimal companyPaidRatio, FixedRule accrualRule,
                                      FixedRule eligibilityRule, boolean migratedLegacy) {}
    public record AttendanceSnapshot(int schemaVersion, ScheduleSnapshot schedule,
                                     LocalDateTime checkIn, LocalDateTime checkOut,
                                     int workingMinutes, int breakMinutes, int lateMinutes,
                                     int earlyLeaveMinutes, int absenceMinutes,
                                     int overtimeMinutes, int approvedOvertimeMinutes,
                                     Long overtimeApprovedBy, LocalDateTime overtimeApprovedAt,
                                     String overtimeApprovalReason,
                                     List<SourceReference> leaveDays, String engineVersion) {}
    public record ClosingSnapshot(int schemaVersion, LocalDate periodStart, LocalDate periodEnd,
                                  int sourceRevision, List<SourceReference> attendance,
                                  List<SourceReference> assignments, List<SourceReference> leaveDays) {}
    public record ReviewedResult(SourceReference result, SourceReference definition,
                                 BigDecimal target, BigDecimal actual, String qualitative,
                                 BigDecimal score, BigDecimal weight, FixedRule scoringRule) {}
    public record ReviewSnapshot(int schemaVersion, List<ReviewedResult> results,
                                 BigDecimal totalWeight, BigDecimal overallScore) {}
    public record EffectiveSegment(LocalDate from, LocalDate to, SourceReference source,
                                   Map<String, String> capturedValues) {}
    public record LeaveDaySnapshot(SourceReference leaveDay, LocalDate leaveDate,
        String segment, LocalTime startTime, LocalTime endTime, int leaveMinutes,
        BigDecimal balanceUnits, int payableMinutes, SourceReference entitlement,
        ScheduleSnapshot schedule, LeavePolicySnapshot policy) {}
    public record LockedReviewSnapshot(SourceReference review, ReviewSnapshot result,
        BigDecimal bonusAmount, String bonusCurrency, Long approvedBy,
        LocalDateTime approvedAt, Long lockedBy, LocalDateTime lockedAt) {}
    public record AdjustmentSnapshot(SourceReference adjustment, SourceReference originalPayslip,
        SourceReference originalItem, SourceReference component, BigDecimal amount,
        String reason, Long approvedBy, LocalDateTime approvedAt) {}
    public record PayrollSourceSnapshot(int schemaVersion,
        List<EffectiveSegment> profiles, List<EffectiveSegment> policies,
        List<EffectiveSegment> compensations, List<EffectiveSegment> contracts,
        List<EffectiveSegment> schedules, List<EffectiveSegment> assignments,
        List<EffectiveSegment> components, List<AttendanceSnapshot> attendance,
        List<LeavePolicySnapshot> leavePolicies, List<LeaveDaySnapshot> leaveDays,
        List<LockedReviewSnapshot> lockedReviews, List<AdjustmentSnapshot> adjustments,
        FixedRule roundingRule, String calculationEngineVersion) {}
    /** Current employee payroll data captured for this calculation, not a live projection. */
    public record PayrollProfileSnapshot(String taxCode, String insuranceNumber,
        BigDecimal insuranceBaseSalary, String taxResidencyType, int dependentCount,
        String bankName, String bankAccountNumber, String bankAccountHolder, String currency) {}
    public record PayslipInputSnapshot(int schemaVersion, BigDecimal standardUnits,
        BigDecimal payableUnits, BigDecimal attendedUnits, int approvedOvertimeMinutes,
        PayrollProfileSnapshot employeeProfile, PayrollSourceSnapshot sourceData) {}
    public record CalculationDetail(int schemaVersion, FixedRule.Method method,
                                     BigDecimal quantity, BigDecimal rate, BigDecimal amount,
                                     List<SourceReference> sources, LocalDate segmentFrom,
                                     LocalDate segmentTo, String explanation) {}
    /** Callers must redact credentials and sensitive values before constructing audit data. */
    public record AuditData(int schemaVersion, Map<String, String> values) {}
}
