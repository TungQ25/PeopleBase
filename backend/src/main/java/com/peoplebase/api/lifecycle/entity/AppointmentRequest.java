package com.peoplebase.api.lifecycle.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.organization.entity.Department;
import com.peoplebase.api.organization.entity.EmployeeAssignment;
import com.peoplebase.api.organization.entity.Position;
import com.peoplebase.api.organization.entity.Team;
import com.peoplebase.api.organization.enums.ManagerType;
import com.peoplebase.api.workflow.entity.WorkflowRequest;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Đề xuất bổ nhiệm chức vụ và/hoặc quản lý đơn vị. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "appointment_requests",
        uniqueConstraints = {@UniqueConstraint(name = "uk_appointment_requests_1", columnNames = {"workflow_request_id"})},
        indexes = {@Index(name = "idx_appointment_requests_workflow_request_id", columnList = "workflow_request_id"), @Index(name = "idx_appointment_requests_from_assignment_id", columnList = "from_assignment_id"), @Index(name = "idx_appointment_requests_to_position_id", columnList = "to_position_id"), @Index(name = "idx_appointment_requests_to_department_id", columnList = "to_department_id"), @Index(name = "idx_appointment_requests_to_team_id", columnList = "to_team_id"), @Index(name = "idx_appointment_requests_manager_department_id", columnList = "manager_department_id"), @Index(name = "idx_appointment_requests_manager_team_id", columnList = "manager_team_id")})
public class AppointmentRequest extends VersionedEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_request_id", nullable = false, unique = true)
    private WorkflowRequest workflowRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "from_assignment_id", nullable = true)
    private EmployeeAssignment fromAssignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "to_position_id", nullable = true)
    private Position toPosition;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "to_department_id", nullable = true)
    private Department toDepartment;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "to_team_id", nullable = true)
    private Team toTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "manager_type", nullable = true, length = 40)
    private ManagerType managerType;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "manager_department_id", nullable = true)
    private Department managerDepartment;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "manager_team_id", nullable = true)
    private Team managerTeam;

    @Column(name = "proposed_effective_date", nullable = true)
    private LocalDate proposedEffectiveDate;

    @Column(name = "approved_effective_date", nullable = true)
    private LocalDate approvedEffectiveDate;

    @Column(name = "reason", nullable = true, length = 2000)
    private String reason;

    @Column(name = "executed_at", nullable = true)
    private LocalDateTime executedAt;
}
