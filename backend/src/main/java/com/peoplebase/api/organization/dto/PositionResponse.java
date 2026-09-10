package com.peoplebase.api.organization.dto;

import java.time.LocalDateTime;

public record PositionResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer level,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
