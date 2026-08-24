package com.peoplebase.api.organization.entity;

import com.peoplebase.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "positions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_positions_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_positions_name", columnNames = "name")
        }
)
public class Position extends BaseEntity {

    @NotBlank
    @Size(max = 30)
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
