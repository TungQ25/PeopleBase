package com.peoplebase.api.leave.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.ScheduleSnapshot;
import com.peoplebase.api.leave.enums.LeaveSegment;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Chi tiết ngày/giờ nghỉ đã tính theo lịch, xử lý nửa ngày và qua năm. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "leave_request_days",
        indexes = {@Index(name = "idx_leave_request_days_request_id", columnList = "request_id"), @Index(name = "idx_leave_request_days_entitlement_id", columnList = "entitlement_id")})
public class LeaveRequestDay extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private LeaveRequest request;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "segment", nullable = false, length = 40)
    private LeaveSegment segment;

    @Column(name = "start_time", nullable = true)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = true)
    private LocalTime endTime;

    @Column(name = "leave_minutes", nullable = false)
    private Integer leaveMinutes;

    @Column(name = "balance_units", nullable = false, precision = 12, scale = 4)
    private BigDecimal balanceUnits;

    @Column(name = "payable_minutes", nullable = false)
    private Integer payableMinutes;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "entitlement_id", nullable = true)
    private LeaveEntitlement entitlement;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule_snapshot", nullable = false, columnDefinition = "jsonb")
    private ScheduleSnapshot scheduleSnapshot;
}
