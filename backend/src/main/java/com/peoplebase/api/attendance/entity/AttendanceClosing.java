package com.peoplebase.api.attendance.entity;

import com.peoplebase.api.attendance.enums.AttendanceClosingStatus;
import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.ClosingSnapshot;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Đợt chốt công toàn công ty theo tháng. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attendance_closings",
        uniqueConstraints = {@UniqueConstraint(name = "uk_attendance_closings_1", columnNames = {"period_start", "period_end"})},
        indexes = {@Index(name = "idx_attendance_closings_closed_by_id", columnList = "closed_by_id")})
public class AttendanceClosing extends VersionedEntity {

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private AttendanceClosingStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "closed_by_id", nullable = true)
    private UserAccount closedBy;

    @Column(name = "closed_at", nullable = true)
    private LocalDateTime closedAt;

    @Column(name = "source_revision", nullable = false)
    private Integer sourceRevision;

    @Column(name = "reopen_reason", nullable = true, length = 255)
    private String reopenReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "closing_snapshot", nullable = true, columnDefinition = "jsonb")
    private ClosingSnapshot closingSnapshot;
}
