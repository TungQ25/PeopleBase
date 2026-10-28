package com.peoplebase.api.leave.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.leave.enums.LeaveUnit;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tài khoản quỹ phép nhân viên–loại nghỉ–năm. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "leave_entitlements",
        uniqueConstraints = {@UniqueConstraint(name = "uk_leave_entitlements_1", columnNames = {"employee_id", "leave_type_id", "entitlement_year"})},
        indexes = {@Index(name = "idx_leave_entitlements_employee_id", columnList = "employee_id"), @Index(name = "idx_leave_entitlements_leave_type_id", columnList = "leave_type_id"), @Index(name = "idx_leave_entitlements_policy_id", columnList = "policy_id")})
public class LeaveEntitlement extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "entitlement_year", nullable = false)
    private Integer entitlementYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 40)
    private LeaveUnit unit;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "policy_id", nullable = true)
    private LeavePolicy policy;

    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "entitled_units", nullable = false, precision = 12, scale = 4, insertable = false, updatable = false)
    private BigDecimal entitledUnits;

    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "carried_units", nullable = false, precision = 12, scale = 4, insertable = false, updatable = false)
    private BigDecimal carriedUnits;

    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "adjusted_units", nullable = false, precision = 12, scale = 4, insertable = false, updatable = false)
    private BigDecimal adjustedUnits;

    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "used_units", nullable = false, precision = 12, scale = 4, insertable = false, updatable = false)
    private BigDecimal usedUnits;

    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "reserved_units", nullable = false, precision = 12, scale = 4, insertable = false, updatable = false)
    private BigDecimal reservedUnits;

    /** Refresh after appending ledger entries; database triggers maintain the read-only totals. */
    @Transient
    public BigDecimal getAvailableUnits() {
        return entitledUnits.add(carriedUnits).add(adjustedUnits).subtract(usedUnits).subtract(reservedUnits);
    }
}
