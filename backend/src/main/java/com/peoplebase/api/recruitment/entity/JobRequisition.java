package com.peoplebase.api.recruitment.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.organization.entity.Department;
import com.peoplebase.api.organization.entity.Position;
import com.peoplebase.api.organization.entity.Team;
import com.peoplebase.api.recruitment.enums.JobRequisitionStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Nhu cầu tuyển có số lượng, người đề xuất và HR xử lý. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "job_requisitions",
        uniqueConstraints = {@UniqueConstraint(name = "uk_job_requisitions_1", columnNames = {"code"})},
        indexes = {@Index(name = "idx_job_requisitions_department_id", columnList = "department_id"), @Index(name = "idx_job_requisitions_team_id", columnList = "team_id"), @Index(name = "idx_job_requisitions_position_id", columnList = "position_id"), @Index(name = "idx_job_requisitions_requested_by_id", columnList = "requested_by_id"), @Index(name = "idx_job_requisitions_handled_by_id", columnList = "handled_by_id")})
public class JobRequisition extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "position_id", nullable = true)
    private Position position;

    @Column(name = "headcount", nullable = true)
    private Integer headcount;

    @Column(name = "reason", nullable = true, length = 2000)
    private String reason;

    @Column(name = "requirements", nullable = true, length = 2000)
    private String requirements;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private UserAccount requestedBy;

    @Column(name = "requested_at", nullable = true)
    private LocalDateTime requestedAt;

    @Column(name = "expected_start_date", nullable = true)
    private LocalDate expectedStartDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private JobRequisitionStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "handled_by_id", nullable = true)
    private UserAccount handledBy;

    @Column(name = "handled_at", nullable = true)
    private LocalDateTime handledAt;

    @Column(name = "decision_reason", nullable = true, length = 2000)
    private String decisionReason;
}
