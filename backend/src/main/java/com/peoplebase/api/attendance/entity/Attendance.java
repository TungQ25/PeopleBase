package com.peoplebase.api.attendance.entity;

import com.peoplebase.api.attendance.enums.AttendanceStatus;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.snapshot.Snapshots.AttendanceSnapshot;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.peoplebase.api.employee.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "attendances",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendances_employee_work_date",
                columnNames = {"employee_id", "work_date"}
        ),
        indexes = {
                @Index(name = "idx_attendances_work_date", columnList = "work_date"),
                @Index(name = "idx_attendances_status", columnList = "status")
        }
)
public class Attendance extends VersionedEntity {

    @NotNull
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Size(max = 500)
    @Column(name = "note", length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_assignment_id")
    private EmployeeScheduleAssignment scheduleAssignment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closing_id")
    private AttendanceClosing closing;

    @Column(name = "scheduled_minutes", nullable = false)
    private Integer scheduledMinutes = 0;
    @Column(name = "working_minutes", nullable = false)
    private Integer workingMinutes = 0;
    @Column(name = "break_minutes", nullable = false)
    private Integer breakMinutes = 0;
    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes = 0;
    @Column(name = "early_leave_minutes", nullable = false)
    private Integer earlyLeaveMinutes = 0;
    @Column(name = "absence_minutes", nullable = false)
    private Integer absenceMinutes = 0;
    @Column(name = "overtime_minutes", nullable = false)
    private Integer overtimeMinutes = 0;
    @Column(name = "approved_overtime_minutes", nullable = false)
    private Integer approvedOvertimeMinutes = 0;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overtime_approved_by_id")
    private UserAccount overtimeApprovedBy;
    @Column(name = "overtime_approved_at")
    private LocalDateTime overtimeApprovedAt;
    @Column(name = "overtime_approval_reason", length = 2000)
    private String overtimeApprovalReason;
    @Column(name = "calculation_version", length = 100)
    private String calculationVersion;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_snapshot", columnDefinition = "jsonb")
    private AttendanceSnapshot calculationSnapshot;
}
