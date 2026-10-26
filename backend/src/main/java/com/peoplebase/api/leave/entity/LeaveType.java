package com.peoplebase.api.leave.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.leave.enums.LeavePaymentSource;
import com.peoplebase.api.leave.enums.LeaveUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Danh mục loại nghỉ do HR quản lý. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "leave_types",
        uniqueConstraints = {@UniqueConstraint(name = "uk_leave_types_1", columnNames = {"code"})})
public class LeaveType extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_source", nullable = false, length = 40)
    private LeavePaymentSource paymentSource;

    @Column(name = "uses_balance", nullable = false)
    private boolean usesBalance = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 40)
    private LeaveUnit unit;

    @Column(name = "active", nullable = false)
    private boolean active = false;

    @Column(name = "description", nullable = true, length = 2000)
    private String description;
}
