package com.peoplebase.api.attendance.entity;

import com.peoplebase.api.attendance.enums.CalendarDayType;
import com.peoplebase.api.common.entity.VersionedEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ngoại lệ lịch công ty: lễ/nghỉ/bù hoặc đổi thời gian của một ngày. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "work_calendar_days",
        uniqueConstraints = {@UniqueConstraint(name = "uk_work_calendar_days_1", columnNames = {"calendar_date"})})
public class WorkCalendarDay extends VersionedEntity {

    @Column(name = "calendar_date", nullable = false)
    private LocalDate calendarDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 40)
    private CalendarDayType dayType;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "override_working_day", nullable = true)
    private Boolean overrideWorkingDay;

    @Column(name = "override_minutes", nullable = true)
    private Integer overrideMinutes;

    @Column(name = "company_paid_holiday", nullable = false)
    private boolean companyPaidHoliday = false;

    @Column(name = "note", nullable = true, length = 2000)
    private String note;

    @Column(name = "override_start_time", nullable = true)
    private LocalTime overrideStartTime;

    @Column(name = "override_end_time", nullable = true)
    private LocalTime overrideEndTime;

    @Column(name = "override_break_start", nullable = true)
    private LocalTime overrideBreakStart;

    @Column(name = "override_break_end", nullable = true)
    private LocalTime overrideBreakEnd;
}
