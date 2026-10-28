package com.peoplebase.api.performance.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.common.snapshot.Snapshots.FixedRule;
import com.peoplebase.api.performance.enums.KPIDirection;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Danh mục tiêu chí và quy tắc đo có version. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "kpi_definitions",
        uniqueConstraints = {@UniqueConstraint(name = "uk_kpi_definitions_1", columnNames = {"code", "revision"})})
public class KPIDefinition extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "revision", nullable = false)
    private Integer revision;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", nullable = true, length = 2000)
    private String description;

    @Column(name = "unit", nullable = false, length = 255)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 40)
    private KPIDirection direction;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scoring_rule", nullable = false, columnDefinition = "jsonb")
    private FixedRule scoringRule;

    @Column(name = "active", nullable = false)
    private boolean active = false;
}
