package com.peoplebase.api.organization.dto;

import java.time.LocalDateTime;

public record DepartmentResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
