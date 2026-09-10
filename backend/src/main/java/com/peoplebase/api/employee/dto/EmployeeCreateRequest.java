package com.peoplebase.api.employee.dto;

import com.peoplebase.api.employee.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeCreateRequest(
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Mã nhân viên chỉ gồm chữ, số, gạch dưới hoặc gạch ngang.")
        String employeeCode,
        @NotBlank @Size(max = 75) String firstName,
        @NotBlank @Size(max = 75) String lastName,
        @Past LocalDate dateOfBirth,
        Gender gender,
        @Size(max = 20) String phone,
        @Email @Size(max = 150) String personalEmail,
        @NotBlank @Email @Size(max = 150) String companyEmail,
        @Size(max = 500) String address,
        @NotNull LocalDate hireDate,
        @NotNull Long departmentId,
        @NotNull Long positionId,
        Long managerId
) {
}
