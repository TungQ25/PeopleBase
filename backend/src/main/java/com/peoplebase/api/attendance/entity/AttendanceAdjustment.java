package com.peoplebase.api.attendance.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.AppendOnlyEntity;
import com.peoplebase.api.common.snapshot.Snapshots.AttendanceSnapshot;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Lịch sử HR sửa công có lý do. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "attendance_adjustments",
        indexes = {@Index(name = "idx_attendance_adjustments_attendance_id", columnList = "attendance_id"), @Index(name = "idx_attendance_adjustments_actor_id", columnList = "actor_id")})
public class AttendanceAdjustment extends AppendOnlyEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private UserAccount actor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_data", nullable = false, columnDefinition = "jsonb")
    private AttendanceSnapshot oldData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data", nullable = false, columnDefinition = "jsonb")
    private AttendanceSnapshot newData;

    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;

    @Column(name = "adjusted_at", nullable = false)
    private LocalDateTime adjustedAt;
}
