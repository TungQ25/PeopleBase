package com.peoplebase.api.attendance.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Gán lịch cho nhân viên theo thời gian. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employee_schedule_assignments",
        indexes = {@Index(name = "idx_employee_schedule_assignments_employee_id", columnList = "employee_id"), @Index(name = "idx_employee_schedule_assignments_schedule_id", columnList = "schedule_id")})
public class EmployeeScheduleAssignment extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private WorkSchedule schedule;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = true)
    private LocalDate effectiveTo;
}
