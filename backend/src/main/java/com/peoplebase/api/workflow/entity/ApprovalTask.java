package com.peoplebase.api.workflow.entity;

import com.peoplebase.api.auth.entity.Permission;
import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.workflow.enums.ApprovalTaskStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Một bước phê duyệt của một request, có thể định tuyến lại khi người duyệt vắng. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "approval_tasks",
        uniqueConstraints = {@UniqueConstraint(name = "uk_approval_tasks_1", columnNames = {"request_id", "step_no"})},
        indexes = {@Index(name = "idx_approval_tasks_request_id", columnList = "request_id"), @Index(name = "idx_approval_tasks_required_permission_id", columnList = "required_permission_id"), @Index(name = "idx_approval_tasks_assigned_to_id", columnList = "assigned_to_id")})
public class ApprovalTask extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private WorkflowRequest request;

    @Column(name = "step_no", nullable = false)
    private Integer stepNo;

    @Column(name = "step_code", nullable = false, length = 50)
    private String stepCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "required_permission_id", nullable = false)
    private Permission requiredPermission;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "assigned_to_id", nullable = true)
    private UserAccount assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private ApprovalTaskStatus status;

    @Column(name = "assigned_at", nullable = true)
    private LocalDateTime assignedAt;

    @Column(name = "due_at", nullable = true)
    private LocalDateTime dueAt;

    @Column(name = "completed_at", nullable = true)
    private LocalDateTime completedAt;

    @Column(name = "routing_reason", nullable = true, length = 2000)
    private String routingReason;
}
