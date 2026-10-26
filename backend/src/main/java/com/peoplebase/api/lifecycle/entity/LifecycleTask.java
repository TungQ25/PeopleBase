package com.peoplebase.api.lifecycle.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.lifecycle.enums.LifecycleTaskStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Nhiệm vụ tiếp nhận/bàn giao được giao. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lifecycle_tasks",
        indexes = {@Index(name = "idx_lifecycle_tasks_lifecycle_case_id", columnList = "lifecycle_case_id"), @Index(name = "idx_lifecycle_tasks_assigned_employee_id", columnList = "assigned_employee_id"), @Index(name = "idx_lifecycle_tasks_assigned_account_id", columnList = "assigned_account_id"), @Index(name = "idx_lifecycle_tasks_verified_by_id", columnList = "verified_by_id"), @Index(name = "idx_lifecycle_tasks_completed_by_id", columnList = "completed_by_id"), @Index(name = "idx_lifecycle_tasks_waived_by_id", columnList = "waived_by_id")})
public class LifecycleTask extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lifecycle_case_id", nullable = false)
    private LifecycleCase lifecycleCase;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = true, length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "assigned_employee_id", nullable = true)
    private Employee assignedEmployee;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "assigned_account_id", nullable = true)
    private UserAccount assignedAccount;

    @Column(name = "due_date", nullable = true)
    private LocalDate dueDate;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private LifecycleTaskStatus status;

    @Column(name = "completed_at", nullable = true)
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "verified_by_id", nullable = true)
    private UserAccount verifiedBy;

    @Column(name = "verified_at", nullable = true)
    private LocalDateTime verifiedAt;

    @Column(name = "evidence_reference", nullable = true, length = 255)
    private String evidenceReference;

    @Column(name = "note", nullable = true, length = 2000)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "completed_by_id", nullable = true)
    private UserAccount completedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "waived_by_id", nullable = true)
    private UserAccount waivedBy;

    @Column(name = "waived_at", nullable = true)
    private LocalDateTime waivedAt;

    @Column(name = "waiver_reason", nullable = true, length = 2000)
    private String waiverReason;
}
