package com.peoplebase.api.performance.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Kết quả chấm từng KPI, giữ phiên bản khi sửa. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "kpi_results",
        uniqueConstraints = {@UniqueConstraint(name = "uk_kpi_results_1", columnNames = {"assignment_id", "revision"})},
        indexes = {@Index(name = "idx_kpi_results_assignment_id", columnList = "assignment_id"), @Index(name = "idx_kpi_results_evaluator_id", columnList = "evaluator_id")})
public class KPIResult extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private EmployeeKPIAssignment assignment;

    @Column(name = "revision", nullable = false)
    private Integer revision;

    @Column(name = "actual_value", nullable = true, precision = 12, scale = 4)
    private BigDecimal actualValue;

    @Column(name = "qualitative_result", nullable = true, length = 255)
    private String qualitativeResult;

    @Column(name = "score", nullable = false, precision = 12, scale = 4)
    private BigDecimal score;

    @Column(name = "evidence_reference", nullable = true, length = 255)
    private String evidenceReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private UserAccount evaluator;

    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt;

    @Column(name = "comment", nullable = true, length = 2000)
    private String comment;

    @Column(name = "final_result", nullable = false)
    private boolean finalResult = false;
}
