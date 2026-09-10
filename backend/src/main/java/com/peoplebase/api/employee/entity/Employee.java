package com.peoplebase.api.employee.entity;

import com.peoplebase.api.common.entity.BaseEntity;
import com.peoplebase.api.employee.enums.EmployeeStatus;
import com.peoplebase.api.employee.enums.Gender;
import com.peoplebase.api.organization.entity.Department;
import com.peoplebase.api.organization.entity.Position;
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employees_employee_code", columnNames = "employee_code"),
                @UniqueConstraint(name = "uk_employees_company_email", columnNames = "company_email")
        },
        indexes = {
                @Index(name = "idx_employees_department_id", columnList = "department_id"),
                @Index(name = "idx_employees_position_id", columnList = "position_id"),
                @Index(name = "idx_employees_manager_id", columnList = "manager_id")
        }
)
public class Employee extends BaseEntity {

    @NotBlank
    @Size(max = 30)
    @Column(name = "employee_code", nullable = false, length = 30)
    private String employeeCode;

    @NotBlank
    @Size(max = 150)
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @NotBlank
    @Size(max = 75)
    @Column(name = "first_name", nullable = false, length = 75)
    private String firstName;

    @NotBlank
    @Size(max = 75)
    @Column(name = "last_name", nullable = false, length = 75)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Email
    @Size(max = 150)
    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(name = "company_email", nullable = false, length = 150)
    private String companyEmail;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;
}
