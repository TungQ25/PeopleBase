package com.peoplebase.api.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DepartmentCreateRequest(
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Mã phòng ban chỉ gồm chữ, số, gạch dưới hoặc gạch ngang.")
        String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description
) {
}
