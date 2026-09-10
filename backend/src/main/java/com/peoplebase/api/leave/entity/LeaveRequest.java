package com.peoplebase.api.leave.entity;

import com.peoplebase.api.common.entity.BaseEntity;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.leave.enums.LeaveRequestStatus;
import com.peoplebase.api.leave.enums.LeaveType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
        name = "leave_requests",
        indexes = {
                @Index(name = "idx_leave_requests_requester_id", columnList = "requester_id"),
                @Index(name = "idx_leave_requests_approver_id", columnList = "approver_id"),
                @Index(name = "idx_leave_requests_status", columnList = "status"),
                @Index(name = "idx_leave_requests_start_date", columnList = "start_date"),
                @Index(name = "idx_leave_requests_end_date", columnList = "end_date")
        }
)
public class LeaveRequest extends BaseEntity {

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveType leaveType;

    @NotBlank
    @Size(max = 1000)
    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeaveRequestStatus status = LeaveRequestStatus.PENDING;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Employee requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Employee approver;

    @Size(max = 1000)
    @Column(name = "decision_note", length = 1000)
    private String decisionNote;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;
}
