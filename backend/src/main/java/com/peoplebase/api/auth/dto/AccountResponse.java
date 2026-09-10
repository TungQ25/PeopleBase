package com.peoplebase.api.auth.dto;

import com.peoplebase.api.auth.enums.AccountStatus;
import com.peoplebase.api.auth.enums.Role;

import java.time.LocalDateTime;

public record AccountResponse(
        Long accountId,
        String username,
        Role role,
        AccountStatus status,
        Long employeeId,
        String employeeCode,
        String fullName,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
