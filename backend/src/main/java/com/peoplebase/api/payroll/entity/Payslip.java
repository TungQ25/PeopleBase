package com.peoplebase.api.payroll.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.attendance.entity.AttendanceClosing;
import com.peoplebase.api.performance.entity.PerformanceReview;
import com.peoplebase.api.common.snapshot.Snapshots.PayslipInputSnapshot;
import com.peoplebase.api.payroll.enums.PayrollInputType;
import com.peoplebase.api.payroll.enums.PayrollPaymentStatus;
import com.peoplebase.api.payroll.enums.PayrollPaymentMethod;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One immutable calculation per employee/period, with a separate single-payment state. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payslips",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payslips_employee_period", columnNames = {"employee_id", "period_id"}),
                @UniqueConstraint(name = "uk_payslips_payment_key", columnNames = "payment_idempotency_key")},
        indexes = {@Index(name = "idx_payslips_period_id", columnList = "period_id"),
                @Index(name = "idx_payslips_employee_id", columnList = "employee_id")})
public class Payslip extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    private PayrollPeriod period;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_snapshot", nullable = false, columnDefinition = "jsonb")
    private PayslipInputSnapshot inputSnapshot;

    @Column(name = "source_hash", nullable = false, length = 64)
    private String sourceHash;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    @Column(name = "calculation_engine_version", nullable = false, length = 255)
    private String calculationEngineVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 40)
    private PayrollInputType inputType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_closing_id")
    private AttendanceClosing attendanceClosing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_review_id")
    private PerformanceReview performanceReview;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "gross_pay", nullable = false, precision = 18, scale = 2)
    private BigDecimal grossPay;

    @Column(name = "employee_deductions", nullable = false, precision = 18, scale = 2)
    private BigDecimal employeeDeductions;

    @Column(name = "employer_contributions", nullable = false, precision = 18, scale = 2)
    private BigDecimal employerContributions;

    @Column(name = "net_pay", nullable = false, precision = 18, scale = 2)
    private BigDecimal netPay;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 40)
    private PayrollPaymentStatus paymentStatus = PayrollPaymentStatus.UNPAID;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 40)
    private PayrollPaymentMethod paymentMethod;

    @Column(name = "payment_reference", length = 255)
    private String paymentReference;

    @Column(name = "payment_evidence_reference", length = 255)
    private String paymentEvidenceReference;

    @Column(name = "payment_idempotency_key", length = 255)
    private String paymentIdempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_id")
    private UserAccount paidBy;
}
