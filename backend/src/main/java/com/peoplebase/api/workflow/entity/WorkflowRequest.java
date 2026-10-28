package com.peoplebase.api.workflow.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.RoutingSnapshot;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.workflow.enums.WorkflowRequestStatus;
import com.peoplebase.api.workflow.enums.WorkflowRequestType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Hồ sơ workflow chung, giữ identity và trạng thái đơn. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "workflow_requests",
        uniqueConstraints = {@UniqueConstraint(name = "uk_workflow_requests_1", columnNames = {"request_code"})},
        indexes = {@Index(name = "idx_workflow_requests_target_employee_id", columnList = "target_employee_id"), @Index(name = "idx_workflow_requests_submitted_by_id", columnList = "submitted_by_id")})
public class WorkflowRequest extends VersionedEntity {

    @Column(name = "request_code", nullable = false, length = 50)
    private String requestCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 40)
    private WorkflowRequestType requestType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_employee_id", nullable = false)
    private Employee targetEmployee;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "submitted_by_id", nullable = true)
    private UserAccount submittedBy;

    @Column(name = "submitted_at", nullable = true)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private WorkflowRequestStatus status;

    @Column(name = "current_step_no", nullable = true)
    private Integer currentStepNo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "routing_snapshot", nullable = true, columnDefinition = "jsonb")
    private RoutingSnapshot routingSnapshot;

    @Column(name = "finalized_at", nullable = true)
    private LocalDateTime finalizedAt;

    @Column(name = "cancelled_at", nullable = true)
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", nullable = true, length = 2000)
    private String cancellationReason;
}
