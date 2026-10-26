package com.peoplebase.api.organization.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.workflow.entity.WorkflowRequest;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employee_assignments",
        indexes = {@Index(name = "idx_employee_assignments_employee_id", columnList = "employee_id"), @Index(name = "idx_employee_assignments_department_id", columnList = "department_id"), @Index(name = "idx_employee_assignments_team_id", columnList = "team_id"), @Index(name = "idx_employee_assignments_position_id", columnList = "position_id"), @Index(name = "idx_employee_assignments_direct_manager_id", columnList = "direct_manager_id"), @Index(name = "idx_employee_assignments_source_request_id", columnList = "source_request_id")})
public class EmployeeAssignment extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "direct_manager_id", nullable = true)
    private Employee directManager;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = true)
    private LocalDate effectiveTo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "source_request_id", nullable = true)
    private WorkflowRequest sourceRequest;

    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;
}
