package com.peoplebase.api.performance.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.performance.enums.KPIAssignmentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Giao target và trọng số KPI cho một nhân viên trong kỳ. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employee_kpi_assignments",
        uniqueConstraints = {@UniqueConstraint(name = "uk_employee_kpi_assignments_1", columnNames = {"cycle_id", "employee_id", "definition_id"})},
        indexes = {@Index(name = "idx_employee_kpi_assignments_cycle_id", columnList = "cycle_id"), @Index(name = "idx_employee_kpi_assignments_employee_id", columnList = "employee_id"), @Index(name = "idx_employee_kpi_assignments_definition_id", columnList = "definition_id"), @Index(name = "idx_employee_kpi_assignments_assigned_by_id", columnList = "assigned_by_id")})
public class EmployeeKPIAssignment extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private PerformanceCycle cycle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_id", nullable = false)
    private KPIDefinition definition;

    @Column(name = "target_value", nullable = true, precision = 12, scale = 4)
    private BigDecimal targetValue;

    @Column(name = "weight", nullable = false, precision = 12, scale = 4)
    private BigDecimal weight;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private UserAccount assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "due_date", nullable = true)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private KPIAssignmentStatus status;
}
