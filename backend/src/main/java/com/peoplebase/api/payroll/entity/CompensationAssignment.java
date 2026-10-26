package com.peoplebase.api.payroll.entity;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.common.entity.VersionedEntity;
import com.peoplebase.api.contract.entity.Contract;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.workflow.entity.WorkflowRequest;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mức lương cơ bản theo thời gian, tách khỏi phiếu lương. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "compensation_assignments",
        indexes = {@Index(name = "idx_compensation_assignments_employee_id", columnList = "employee_id"), @Index(name = "idx_compensation_assignments_contract_id", columnList = "contract_id"), @Index(name = "idx_compensation_assignments_approved_by_id", columnList = "approved_by_id"), @Index(name = "idx_compensation_assignments_source_request_id", columnList = "source_request_id")})
public class CompensationAssignment extends VersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "contract_id", nullable = true)
    private Contract contract;

    @Column(name = "base_salary", nullable = false, precision = 18, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = true)
    private LocalDate effectiveTo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approved_by_id", nullable = false)
    private UserAccount approvedBy;

    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "source_request_id", nullable = true)
    private WorkflowRequest sourceRequest;

    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;
}
