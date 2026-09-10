package com.peoplebase.api.auth.dto;

import com.peoplebase.api.auth.enums.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull Role role) {
}
