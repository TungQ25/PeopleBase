package com.peoplebase.api.payroll.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.FixedRule;
import com.peoplebase.api.payroll.enums.UnpaidLeaveTreatment;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Phiên bản chính sách tính lương, lịch công và làm tròn. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payroll_policies",
        uniqueConstraints = {@UniqueConstraint(name = "uk_payroll_policies_1", columnNames = {"code", "revision"})},
        indexes = {@Index(name = "idx_payroll_policies_approved_by_id", columnList = "approved_by_id")})
public class PayrollPolicy extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "revision", nullable = false)
    private Integer revision;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = true)
    private LocalDate effectiveTo;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rounding_rule", nullable = false, columnDefinition = "jsonb")
    private FixedRule roundingRule;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "base_pay_rule", nullable = false, columnDefinition = "jsonb")
    private FixedRule basePayRule;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "component_rules", nullable = false, columnDefinition = "jsonb")
    private FixedRule componentRules;

    @Enumerated(EnumType.STRING)
    @Column(name = "unpaid_leave_treatment", nullable = false, length = 40)
    private UnpaidLeaveTreatment unpaidLeaveTreatment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approved_by_id", nullable = false)
    private UserAccount approvedBy;

    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;
}
