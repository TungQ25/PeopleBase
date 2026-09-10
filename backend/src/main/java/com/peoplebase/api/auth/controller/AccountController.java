package com.peoplebase.api.auth.controller;

import com.peoplebase.api.auth.dto.AccountEmployeeAssignmentRequest;
import com.peoplebase.api.auth.dto.AccountResponse;
import com.peoplebase.api.auth.dto.AccountStatusUpdateRequest;
import com.peoplebase.api.auth.dto.ChangeRoleRequest;
import com.peoplebase.api.auth.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountResponse> getAll() {
        return accountService.getAll();
    }

    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable Long id) {
        return accountService.getById(id);
    }

    @PatchMapping("/{id}/role")
    public AccountResponse changeRole(@PathVariable Long id, @Valid @RequestBody ChangeRoleRequest request) {
        return accountService.changeRole(id, request);
    }

    @PatchMapping("/{id}/status")
    public AccountResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AccountStatusUpdateRequest request,
            Authentication authentication
    ) {
        return accountService.updateStatus(id, request, authentication.getName());
    }

    @PatchMapping("/{id}/employee")
    public AccountResponse assignEmployee(
            @PathVariable Long id,
            @RequestBody AccountEmployeeAssignmentRequest request
    ) {
        return accountService.assignEmployee(id, request);
    }
}
