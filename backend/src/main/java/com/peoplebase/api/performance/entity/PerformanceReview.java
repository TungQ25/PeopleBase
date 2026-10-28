package com.peoplebase.api.performance.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.ReviewSnapshot;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.performance.enums.PerformanceReviewStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Kết luận đánh giá nhân viên trong kỳ; lưu tập result được chốt. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "performance_reviews",
        uniqueConstraints = {@UniqueConstraint(name = "uk_performance_reviews_1", columnNames = {"cycle_id", "employee_id"})},
        indexes = {@Index(name = "idx_performance_reviews_cycle_id", columnList = "cycle_id"), @Index(name = "idx_performance_reviews_employee_id", columnList = "employee_id"), @Index(name = "idx_performance_reviews_reviewer_id", columnList = "reviewer_id"), @Index(name = "idx_performance_reviews_approved_by_id", columnList = "approved_by_id"), @Index(name = "idx_performance_reviews_locked_by_id", columnList = "locked_by_id")})
public class PerformanceReview extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private PerformanceCycle cycle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private UserAccount reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private PerformanceReviewStatus status;

    @Column(name = "overall_score", nullable = true, precision = 12, scale = 4)
    private BigDecimal overallScore;

    @Column(name = "rating", nullable = true, length = 255)
    private String rating;

    @Column(name = "summary", nullable = true, length = 2000)
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_snapshot", nullable = true, columnDefinition = "jsonb")
    private ReviewSnapshot resultSnapshot;

    @Column(name = "bonus_amount", nullable = true, precision = 18, scale = 2)
    private BigDecimal bonusAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "approved_by_id", nullable = true)
    private UserAccount approvedBy;

    @Column(name = "approved_at", nullable = true)
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "locked_by_id", nullable = true)
    private UserAccount lockedBy;

    @Column(name = "locked_at", nullable = true)
    private LocalDateTime lockedAt;

    @Column(name = "bonus_currency", nullable = true, length = 3)
    private String bonusCurrency;
}
