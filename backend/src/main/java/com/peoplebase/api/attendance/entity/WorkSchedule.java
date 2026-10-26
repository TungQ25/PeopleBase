package com.peoplebase.api.attendance.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mẫu lịch làm việc có phiên bản. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "work_schedules",
        uniqueConstraints = {@UniqueConstraint(name = "uk_work_schedules_1", columnNames = {"code", "revision"})})
public class WorkSchedule extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "revision", nullable = false)
    private Integer revision;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = true)
    private LocalDate effectiveTo;

    @Column(name = "active", nullable = false)
    private boolean active = false;
}
