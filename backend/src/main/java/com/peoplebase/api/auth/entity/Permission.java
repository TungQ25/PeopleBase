package com.peoplebase.api.auth.entity;

import com.peoplebase.api.common.entity.VersionedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Danh mục quyền thao tác chi tiết, không phải chức vụ. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "permissions",
        uniqueConstraints = {@UniqueConstraint(name = "uk_permissions_1", columnNames = {"code"})})
public class Permission extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "module", nullable = false, length = 100)
    private String module;

    @Column(name = "description", nullable = true, length = 2000)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = false;
}
