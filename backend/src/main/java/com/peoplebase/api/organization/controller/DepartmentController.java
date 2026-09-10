package com.peoplebase.api.organization.controller;

import com.peoplebase.api.common.web.ActiveStatusRequest;
import com.peoplebase.api.organization.dto.DepartmentCreateRequest;
import com.peoplebase.api.organization.dto.DepartmentResponse;
import com.peoplebase.api.organization.dto.DepartmentUpdateRequest;
import com.peoplebase.api.organization.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public List<DepartmentResponse> getAll(@RequestParam(required = false) Boolean active) {
        return departmentService.getAll(active);
    }

    @GetMapping("/{id}")
    public DepartmentResponse getById(@PathVariable Long id) {
        return departmentService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateRequest request) {
        return departmentService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public DepartmentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody ActiveStatusRequest request) {
        return departmentService.updateStatus(id, request.active());
    }
}
