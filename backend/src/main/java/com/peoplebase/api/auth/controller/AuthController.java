package com.peoplebase.api.auth.controller;

import com.peoplebase.api.auth.dto.AccountResponse;
import com.peoplebase.api.auth.dto.LoginRequest;
import com.peoplebase.api.auth.dto.LoginResponse;
import com.peoplebase.api.auth.security.JwtTokenService;
import com.peoplebase.api.auth.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var account = accountService.authenticate(request.username(), request.password());
        var token = jwtTokenService.issue(account);
        return new LoginResponse(
                token.value(),
                "Bearer",
                token.expiresAt(),
                accountService.getCurrentAccount(account.getUsername())
        );
    }

    @GetMapping("/me")
    public AccountResponse getCurrentAccount(Authentication authentication) {
        return accountService.getCurrentAccount(authentication.getName());
    }
}
