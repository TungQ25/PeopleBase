package com.peoplebase.api.organization.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.organization.enums.ManagerType;
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
@Table(name = "manager_assignments",
        indexes = {@Index(name = "idx_manager_assignments_manager_id", columnList = "manager_id"), @Index(name = "idx_manager_assignments_department_id", columnList = "department_id"), @Index(name = "idx_manager_assignments_team_id", columnList = "team_id"), @Index(name = "idx_manager_assignments_source_request_id", columnList = "source_request_id")})
public class ManagerAssignment extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private Employee manager;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(name = "manager_type", nullable = false, length = 40)
    private ManagerType managerType;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = true)
    private LocalDate effectiveTo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "source_request_id", nullable = true)
    private WorkflowRequest sourceRequest;
}
