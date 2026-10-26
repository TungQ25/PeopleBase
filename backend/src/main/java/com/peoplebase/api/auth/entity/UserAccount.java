package com.peoplebase.api.auth.entity;

import com.peoplebase.api.auth.enums.Role;
import com.peoplebase.api.auth.enums.AccountStatus;
import com.peoplebase.api.common.entity.VersionedEntity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import com.peoplebase.api.employee.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DynamicUpdate
@Table(
        name = "user_accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_accounts_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_user_accounts_employee_id", columnNames = "employee_id")
        }
)
public class UserAccount extends VersionedEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Setter(lombok.AccessLevel.NONE)
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<AccountRole> accountRoles = new ArrayList<>();

    /** Compatibility projection for existing single-role DTOs. No role column is written. */
    @Transient
    public Role getRole() {
        return getActiveRoles().stream().min(java.util.Comparator.naturalOrder()).orElse(null);
    }

    @Transient
    public List<Role> getActiveRoles() {
        LocalDateTime now = LocalDateTime.now();
        return accountRoles.stream()
                .filter(r -> !r.getValidFrom().isAfter(now))
                .filter(r -> r.getValidTo() == null || r.getValidTo().isAfter(now))
                .map(AccountRole::getRole).distinct().toList();
    }

    /** The old endpoint replaces the active role set; closed grants remain as history. */
    public void setRole(Role role) {
        java.util.Objects.requireNonNull(role, "role");
        LocalDateTime now = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        for (AccountRole grant : accountRoles) {
            if (grant.getValidFrom().equals(now)) now = now.plusNanos(1000);
        }
        for (AccountRole grant : accountRoles) {
            if (grant.getValidTo() == null || grant.getValidTo().isAfter(now)) {
                // The compatibility API cannot replace scheduled future grants.
                if (grant.getValidFrom().isAfter(now)) {
                    throw new IllegalStateException("Scheduled role grants require explicit revocation");
                }
                grant.setValidTo(now);
            }
        }
        AccountRole grant = new AccountRole();
        grant.setAccount(this);
        grant.setRole(role);
        grant.setValidFrom(now);
        grant.setReason("Assigned through existing account role operation");
        accountRoles.add(grant);
    }

    @Column(name = "token_version", nullable = false)
    private Long tokenVersion = 0L;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true)
    private Employee employee;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
