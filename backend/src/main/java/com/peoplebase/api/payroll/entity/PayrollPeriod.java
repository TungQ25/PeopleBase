package com.peoplebase.api.payroll.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.payroll.enums.PayrollPeriodStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Kỳ lương, quyền duyệt/công bố/khóa tách riêng. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payroll_periods",
        uniqueConstraints = {@UniqueConstraint(name = "uk_payroll_periods_1", columnNames = {"code"})},
        indexes = {@Index(name = "idx_payroll_periods_policy_id", columnList = "policy_id"), @Index(name = "idx_payroll_periods_published_by_id", columnList = "published_by_id"), @Index(name = "idx_payroll_periods_closed_by_id", columnList = "closed_by_id")})
public class PayrollPeriod extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "pay_date", nullable = true)
    private LocalDate payDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "policy_id", nullable = true)
    private PayrollPolicy policy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private PayrollPeriodStatus status = PayrollPeriodStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculated_by_id")
    private UserAccount calculatedBy;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private UserAccount reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", length = 2000)
    private String reviewNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private UserAccount approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "total_gross", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalGross = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "total_net", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalNet = BigDecimal.ZERO;

    @Column(name = "input_locked_at", nullable = true)
    private LocalDateTime inputLockedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "published_by_id", nullable = true)
    private UserAccount publishedBy;

    @Column(name = "published_at", nullable = true)
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "closed_by_id", nullable = true)
    private UserAccount closedBy;

    @Column(name = "closed_at", nullable = true)
    private LocalDateTime closedAt;
}
