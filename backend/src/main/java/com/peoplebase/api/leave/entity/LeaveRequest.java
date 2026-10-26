package com.peoplebase.api.leave.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.LeavePolicySnapshot;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.leave.enums.LeaveUnit;
import com.peoplebase.api.workflow.entity.WorkflowRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;

/** WorkflowRequest owns requester, submission and status; decisions own approval history. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "leave_requests", uniqueConstraints =
        @UniqueConstraint(name = "uk_leave_requests_workflow", columnNames = "workflow_request_id"))
public class LeaveRequest extends VersionedEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_request_id", nullable = false, unique = true)
    private WorkflowRequest workflowRequest;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;
    @Column(name = "reason", length = 1000)
    private String reason;
    @Column(name = "total_units", precision = 12, scale = 4)
    private BigDecimal totalUnits;
    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 40)
    private LeaveUnit unit = LeaveUnit.DAY;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy_snapshot", columnDefinition = "jsonb")
    private LeavePolicySnapshot policySnapshot;

    @Transient
    public Employee getRequester() { return workflowRequest.getTargetEmployee(); }
}
