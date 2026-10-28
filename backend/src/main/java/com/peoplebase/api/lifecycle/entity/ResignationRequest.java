package com.peoplebase.api.lifecycle.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.workflow.entity.WorkflowRequest;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Một đơn nghỉ việc xuyên suốt Manager → HR → hiệu lực. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "resignation_requests",
        uniqueConstraints = {@UniqueConstraint(name = "uk_resignation_requests_1", columnNames = {"workflow_request_id"})},
        indexes = {@Index(name = "idx_resignation_requests_workflow_request_id", columnList = "workflow_request_id")})
public class ResignationRequest extends VersionedEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_request_id", nullable = false, unique = true)
    private WorkflowRequest workflowRequest;

    @Column(name = "proposed_last_working_date", nullable = true)
    private LocalDate proposedLastWorkingDate;

    @Column(name = "approved_last_working_date", nullable = true)
    private LocalDate approvedLastWorkingDate;

    @Column(name = "effective_date", nullable = true)
    private LocalDate effectiveDate;

    @Column(name = "reason", nullable = true, length = 2000)
    private String reason;

    @Column(name = "executed_at", nullable = true)
    private LocalDateTime executedAt;
}
