package com.peoplebase.api.workflow.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.AppendOnlyEntity;
import com.peoplebase.api.workflow.enums.ApprovalDecisionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/** Quyết định bất biến ở bước duyệt. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "approval_decisions",
        uniqueConstraints = {@UniqueConstraint(name = "uk_approval_decisions_1", columnNames = {"task_id"}), @UniqueConstraint(name = "uk_approval_decisions_2", columnNames = {"idempotency_key"})},
        indexes = {@Index(name = "idx_approval_decisions_task_id", columnList = "task_id"), @Index(name = "idx_approval_decisions_actor_id", columnList = "actor_id")})
public class ApprovalDecision extends AppendOnlyEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private ApprovalTask task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private UserAccount actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 40)
    private ApprovalDecisionType decision;

    @Column(name = "note", nullable = true, length = 2000)
    private String note;

    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    @Column(name = "old_status", nullable = false, length = 255)
    private String oldStatus;

    @Column(name = "new_status", nullable = false, length = 255)
    private String newStatus;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;
}
