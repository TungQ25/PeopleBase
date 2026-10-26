package com.peoplebase.api.payroll.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.FixedRule;
import com.peoplebase.api.employee.entity.Employee;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Mức phụ cấp/khoản định kỳ riêng của nhân viên. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employee_pay_component_assignments",
        indexes = {@Index(name = "idx_employee_pay_component_assignments_employee_id", columnList = "employee_id"), @Index(name = "idx_employee_pay_component_assignments_component_id", columnList = "component_id"), @Index(name = "idx_employee_pay_component_assignments_approved_by_id", columnList = "approved_by_id")})
public class EmployeePayComponentAssignment extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_id", nullable = false)
    private PayComponent component;

    @Column(name = "amount", nullable = true, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "rate", nullable = true, precision = 12, scale = 4)
    private BigDecimal rate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule_parameters", nullable = true, columnDefinition = "jsonb")
    private FixedRule ruleParameters;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = true)
    private LocalDate effectiveTo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approved_by_id", nullable = false)
    private UserAccount approvedBy;

    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;
}
