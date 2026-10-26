package com.peoplebase.api.recruitment.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.recruitment.enums.ApplicationStage;
import com.peoplebase.api.recruitment.enums.ApplicationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Một lần ứng tuyển cho một nhu cầu. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "applications",
        indexes = {@Index(name = "idx_applications_candidate_id", columnList = "candidate_id"), @Index(name = "idx_applications_requisition_id", columnList = "requisition_id"), @Index(name = "idx_applications_recruiter_id", columnList = "recruiter_id")})
public class Application extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requisition_id", nullable = false)
    private JobRequisition requisition;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 40)
    private ApplicationStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private ApplicationStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "recruiter_id", nullable = true)
    private UserAccount recruiter;

    @Column(name = "note", nullable = true, length = 2000)
    private String note;
}
