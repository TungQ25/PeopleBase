package com.peoplebase.api.leave.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.FixedRule;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Chính sách cấp phép có phiên bản và hiệu lực. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "leave_policies",
        uniqueConstraints = {@UniqueConstraint(name = "uk_leave_policies_1", columnNames = {"leave_type_id", "revision"})},
        indexes = {@Index(name = "idx_leave_policies_leave_type_id", columnList = "leave_type_id")})
public class LeavePolicy extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "revision", nullable = false)
    private Integer revision;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = true)
    private LocalDate effectiveTo;

    @Column(name = "annual_units", nullable = true, precision = 12, scale = 4)
    private BigDecimal annualUnits;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "accrual_rule", nullable = false, columnDefinition = "jsonb")
    private FixedRule accrualRule;

    @Column(name = "carry_forward_limit", nullable = true, precision = 12, scale = 4)
    private BigDecimal carryForwardLimit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "carry_forward_expiry_rule", nullable = true, columnDefinition = "jsonb")
    private FixedRule carryForwardExpiryRule;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "eligibility_rule", nullable = false, columnDefinition = "jsonb")
    private FixedRule eligibilityRule;

    @Column(name = "active", nullable = false)
    private boolean active = false;
}
