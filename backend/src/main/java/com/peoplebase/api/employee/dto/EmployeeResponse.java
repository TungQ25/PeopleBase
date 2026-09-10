package com.peoplebase.api.employee.dto;

import com.peoplebase.api.employee.enums.EmployeeStatus;
import com.peoplebase.api.employee.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String fullName,
        LocalDate dateOfBirth,
        Gender gender,
        String phone,
        String personalEmail,
        String companyEmail,
        String address,
        LocalDate hireDate,
        LocalDate terminationDate,
        EmployeeStatus status,
        Long departmentId,
        String departmentCode,
        String departmentName,
        Long positionId,
        String positionCode,
        String positionName,
        Long managerId,
        String managerEmployeeCode,
        String managerFullName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
