package com.peoplebase.api.auth.entity;

import com.peoplebase.api.auth.enums.Role;
import com.peoplebase.api.common.entity.VersionedEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Gán một trong bốn vai trò cố định cho tài khoản, có lịch sử hiệu lực. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "account_roles",
        indexes = {@Index(name = "idx_account_roles_account_id", columnList = "account_id"), @Index(name = "idx_account_roles_granted_by_id", columnList = "granted_by_id"), @Index(name = "idx_account_roles_revoked_by_id", columnList = "revoked_by_id")})
public class AccountRole extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private UserAccount account;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 40)
    private Role role;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = true)
    private LocalDateTime validTo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "granted_by_id", nullable = true)
    private UserAccount grantedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "revoked_by_id", nullable = true)
    private UserAccount revokedBy;

    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;
}
