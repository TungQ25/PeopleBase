package com.peoplebase.api.leave.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.AppendOnlyEntity;
import com.peoplebase.api.leave.enums.LeaveBalanceTransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/** Ledger cấp/giữ/dùng/hoàn/điều chỉnh/cắt phép hết hạn. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "leave_balance_transactions",
        uniqueConstraints = {@UniqueConstraint(name = "uk_leave_balance_transactions_1", columnNames = {"idempotency_key"})},
        indexes = {@Index(name = "idx_leave_balance_transactions_entitlement_id", columnList = "entitlement_id"), @Index(name = "idx_leave_balance_transactions_request_id", columnList = "request_id"), @Index(name = "idx_leave_balance_transactions_actor_id", columnList = "actor_id")})
public class LeaveBalanceTransaction extends AppendOnlyEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entitlement_id", nullable = false)
    private LeaveEntitlement entitlement;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "request_id", nullable = true)
    private LeaveRequest request;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 40)
    private LeaveBalanceTransactionType transactionType;

    @Column(name = "entitled_delta", nullable = false, precision = 12, scale = 4)
    private BigDecimal entitledDelta;

    @Column(name = "carried_delta", nullable = false, precision = 12, scale = 4)
    private BigDecimal carriedDelta;

    @Column(name = "adjusted_delta", nullable = false, precision = 12, scale = 4)
    private BigDecimal adjustedDelta;

    @Column(name = "used_delta", nullable = false, precision = 12, scale = 4)
    private BigDecimal usedDelta;

    @Column(name = "reserved_delta", nullable = false, precision = 12, scale = 4)
    private BigDecimal reservedDelta;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "actor_id", nullable = true)
    private UserAccount actor;

    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;
}
