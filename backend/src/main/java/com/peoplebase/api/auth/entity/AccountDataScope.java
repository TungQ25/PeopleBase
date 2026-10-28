package com.peoplebase.api.auth.entity;

import com.peoplebase.api.auth.enums.DataScopeType;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.organization.entity.Department;
import com.peoplebase.api.organization.entity.Team;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Phạm vi dữ liệu của một account cho từng quyền. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "account_data_scopes",
        indexes = {@Index(name = "idx_account_data_scopes_account_role_id", columnList = "account_role_id"), @Index(name = "idx_account_data_scopes_permission_id", columnList = "permission_id"), @Index(name = "idx_account_data_scopes_department_id", columnList = "department_id"), @Index(name = "idx_account_data_scopes_team_id", columnList = "team_id")})
public class AccountDataScope extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_role_id", nullable = false)
    private AccountRole accountRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 40)
    private DataScopeType scopeType;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = true)
    private LocalDate effectiveTo;
}
