package com.peoplebase.api.recruitment.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.recruitment.enums.InterviewResult;
import com.peoplebase.api.recruitment.enums.InterviewStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Lịch và kết quả vòng phỏng vấn. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "interviews",
        uniqueConstraints = {@UniqueConstraint(name = "uk_interviews_1", columnNames = {"application_id", "round_no"})},
        indexes = {@Index(name = "idx_interviews_application_id", columnList = "application_id"), @Index(name = "idx_interviews_interviewer_id", columnList = "interviewer_id")})
public class Interview extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "round_no", nullable = false)
    private Integer roundNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private Employee interviewer;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Column(name = "location_or_link", nullable = true, length = 255)
    private String locationOrLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private InterviewStatus status;

    @Column(name = "score", nullable = true, precision = 12, scale = 4)
    private BigDecimal score;

    @Column(name = "feedback", nullable = true, length = 2000)
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = true, length = 40)
    private InterviewResult result;
}
