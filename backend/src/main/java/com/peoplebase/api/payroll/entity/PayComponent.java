package com.peoplebase.api.payroll.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.payroll.enums.PayCalculationMethod;
import com.peoplebase.api.payroll.enums.PayComponentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Danh mục khoản thu nhập, khấu trừ và phần doanh nghiệp đóng. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pay_components",
        uniqueConstraints = {@UniqueConstraint(name = "uk_pay_components_1", columnNames = {"code"})})
public class PayComponent extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 40)
    private PayComponentType componentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false, length = 40)
    private PayCalculationMethod calculationMethod;

    @Column(name = "taxable", nullable = false)
    private boolean taxable = false;

    @Column(name = "insurance_applicable", nullable = false)
    private boolean insuranceApplicable = false;

    @Column(name = "active", nullable = false)
    private boolean active = false;

    @Column(name = "description", nullable = true, length = 2000)
    private String description;
}
