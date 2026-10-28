package com.peoplebase.api.audit.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.AppendOnlyEntity;
import com.peoplebase.api.common.snapshot.Snapshots.AuditData;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Sự kiện bất biến cho thay đổi nhạy cảm. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "audit_logs",
        indexes = {@Index(name = "idx_audit_logs_actor_id", columnList = "actor_id")})
public class AuditLog extends AppendOnlyEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "actor_id", nullable = true)
    private UserAccount actor;

    @Column(name = "actor_username", nullable = false, length = 100)
    private String actorUsername;

    @Column(name = "action", nullable = false, length = 255)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 255)
    private String entityType;

    @Column(name = "entity_id", nullable = true)
    private Long entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_data", nullable = true, columnDefinition = "jsonb")
    private AuditData beforeData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_data", nullable = true, columnDefinition = "jsonb")
    private AuditData afterData;

    @Column(name = "reason", nullable = true, length = 2000)
    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "correlation_id", nullable = true, length = 255)
    private String correlationId;
}
