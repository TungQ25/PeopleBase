package com.peoplebase.api.lifecycle.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.lifecycle.enums.LifecycleCaseStatus;
import com.peoplebase.api.lifecycle.enums.LifecycleCaseType;
import com.peoplebase.api.recruitment.entity.Offer;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Hồ sơ tiếp nhận hoặc bàn giao/nghỉ việc. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lifecycle_cases",
        uniqueConstraints = {@UniqueConstraint(name = "uk_lifecycle_cases_1", columnNames = {"resignation_request_id"}), @UniqueConstraint(name = "uk_lifecycle_cases_2", columnNames = {"offer_id"})},
        indexes = {@Index(name = "idx_lifecycle_cases_employee_id", columnList = "employee_id"), @Index(name = "idx_lifecycle_cases_resignation_request_id", columnList = "resignation_request_id"), @Index(name = "idx_lifecycle_cases_offer_id", columnList = "offer_id"), @Index(name = "idx_lifecycle_cases_coordinator_id", columnList = "coordinator_id")})
public class LifecycleCase extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", nullable = false, length = 40)
    private LifecycleCaseType caseType;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "resignation_request_id", nullable = true, unique = true)
    private ResignationRequest resignationRequest;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "offer_id", nullable = true, unique = true)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coordinator_id", nullable = false)
    private UserAccount coordinator;

    @Column(name = "planned_start_date", nullable = false)
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date", nullable = true)
    private LocalDate plannedEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private LifecycleCaseStatus status;

    @Column(name = "completed_at", nullable = true)
    private LocalDateTime completedAt;
}
