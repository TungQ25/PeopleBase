package com.peoplebase.api.employee.dto;

import com.peoplebase.api.employee.enums.EmployeeStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EmployeeStatusUpdateRequest(@NotNull EmployeeStatus status, LocalDate terminationDate) {
}
