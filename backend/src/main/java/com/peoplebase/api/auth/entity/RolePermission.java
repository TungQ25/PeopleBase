package com.peoplebase.api.auth.entity;

import com.peoplebase.api.auth.enums.Role;
import com.peoplebase.api.common.entity.VersionedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Gán quyền cho bốn role cố định. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "role_permissions",
        uniqueConstraints = {@UniqueConstraint(name = "uk_role_permissions_1", columnNames = {"role", "permission_id"})},
        indexes = {@Index(name = "idx_role_permissions_permission_id", columnList = "permission_id")})
public class RolePermission extends VersionedEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 40)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}
