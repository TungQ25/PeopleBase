package com.peoplebase.api.organization.controller;

import com.peoplebase.api.common.web.ActiveStatusRequest;
import com.peoplebase.api.organization.dto.PositionCreateRequest;
import com.peoplebase.api.organization.dto.PositionResponse;
import com.peoplebase.api.organization.dto.PositionUpdateRequest;
import com.peoplebase.api.organization.service.PositionService;
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
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    public List<PositionResponse> getAll(@RequestParam(required = false) Boolean active) {
        return positionService.getAll(active);
    }

    @GetMapping("/{id}")
    public PositionResponse getById(@PathVariable Long id) {
        return positionService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<PositionResponse> create(@Valid @RequestBody PositionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(positionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public PositionResponse update(@PathVariable Long id, @Valid @RequestBody PositionUpdateRequest request) {
        return positionService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public PositionResponse updateStatus(@PathVariable Long id, @Valid @RequestBody ActiveStatusRequest request) {
        return positionService.updateStatus(id, request.active());
    }
}
