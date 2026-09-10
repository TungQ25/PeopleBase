package com.peoplebase.api.employee.controller;

import com.peoplebase.api.employee.dto.EmployeeCreateRequest;
import com.peoplebase.api.employee.dto.EmployeeCreationResponse;
import com.peoplebase.api.employee.dto.EmployeeResponse;
import com.peoplebase.api.employee.dto.EmployeeStatusUpdateRequest;
import com.peoplebase.api.employee.dto.EmployeeUpdateRequest;
import com.peoplebase.api.employee.enums.EmployeeStatus;
import com.peoplebase.api.employee.service.EmployeeAccountProvisioning;
import com.peoplebase.api.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeAccountProvisioning employeeAccountProvisioning;

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public List<EmployeeResponse> getAll(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status
    ) {
        return employeeService.getAll(departmentId, status);
    }

    @GetMapping("/me")
    public EmployeeResponse getMe(Authentication authentication) {
        return employeeService.getById(employeeAccountProvisioning.requireEmployeeId(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<EmployeeCreationResponse> create(@Valid @RequestBody EmployeeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest request) {
        return employeeService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('HR')")
    public EmployeeResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeStatusUpdateRequest request
    ) {
        return employeeService.updateStatus(id, request);
    }
}
