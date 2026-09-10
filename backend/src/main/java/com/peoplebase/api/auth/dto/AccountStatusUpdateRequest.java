package com.peoplebase.api.auth.dto;

import com.peoplebase.api.auth.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record AccountStatusUpdateRequest(@NotNull AccountStatus status) {
}
