package com.peoplebase.api.organization.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "teams",
        uniqueConstraints = {@UniqueConstraint(name = "uk_teams_1", columnNames = {"department_id", "code"})},
        indexes = {@Index(name = "idx_teams_department_id", columnList = "department_id")})
public class Team extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "description", nullable = true, length = 2000)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = false;
}
