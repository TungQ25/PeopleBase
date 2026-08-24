package com.peoplebase.api.contract.entity;

import com.peoplebase.api.common.entity.BaseEntity;
import com.peoplebase.api.contract.enums.ContractStatus;
import com.peoplebase.api.contract.enums.ContractType;
import com.peoplebase.api.employee.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "contracts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_contracts_contract_number",
                columnNames = "contract_number"
        ),
        indexes = {
                @Index(name = "idx_contracts_employee_id", columnList = "employee_id"),
                @Index(name = "idx_contracts_status", columnList = "status"),
                @Index(name = "idx_contracts_end_date", columnList = "end_date")
        }
)
public class Contract extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "contract_number", nullable = false, length = 50)
    private String contractNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ContractType type;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal basicSalary;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContractStatus status = ContractStatus.DRAFT;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Size(max = 1000)
    @Column(name = "note", length = 1000)
    private String note;
}
