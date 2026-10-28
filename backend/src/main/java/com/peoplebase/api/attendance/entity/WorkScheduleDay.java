package com.peoplebase.api.attendance.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Giờ làm và khoảng nghỉ theo ngày trong tuần của mẫu lịch. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "work_schedule_days",
        uniqueConstraints = {@UniqueConstraint(name = "uk_work_schedule_days_1", columnNames = {"schedule_id", "day_of_week"})},
        indexes = {@Index(name = "idx_work_schedule_days_schedule_id", columnList = "schedule_id")})
public class WorkScheduleDay extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private WorkSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 40)
    private DayOfWeek dayOfWeek;

    @Column(name = "working_day", nullable = false)
    private boolean workingDay = false;

    @Column(name = "start_time", nullable = true)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = true)
    private LocalTime endTime;

    @Column(name = "break_start", nullable = true)
    private LocalTime breakStart;

    @Column(name = "break_end", nullable = true)
    private LocalTime breakEnd;

    @Column(name = "scheduled_minutes", nullable = false)
    private Integer scheduledMinutes;

    @Column(name = "late_tolerance_minutes", nullable = false)
    private Integer lateToleranceMinutes;

    @Column(name = "early_tolerance_minutes", nullable = false)
    private Integer earlyToleranceMinutes;
}
