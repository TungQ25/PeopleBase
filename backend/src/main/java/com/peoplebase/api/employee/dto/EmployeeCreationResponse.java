package com.peoplebase.api.employee.dto;

public record EmployeeCreationResponse(
        EmployeeResponse employee,
        String accountUsername,
        String temporaryPassword
) {
}
