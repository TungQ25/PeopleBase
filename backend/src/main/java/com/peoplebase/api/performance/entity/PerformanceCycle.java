package com.peoplebase.api.performance.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.performance.enums.PerformanceCycleStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Kỳ đánh giá và trạng thái đóng kết quả. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "performance_cycles",
        uniqueConstraints = {@UniqueConstraint(name = "uk_performance_cycles_1", columnNames = {"code"})},
        indexes = {@Index(name = "idx_performance_cycles_locked_by_id", columnList = "locked_by_id")})
public class PerformanceCycle extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private PerformanceCycleStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "locked_by_id", nullable = true)
    private UserAccount lockedBy;

    @Column(name = "locked_at", nullable = true)
    private LocalDateTime lockedAt;
}
