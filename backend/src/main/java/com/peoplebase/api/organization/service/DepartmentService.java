package com.peoplebase.api.organization.service;

import com.peoplebase.api.common.exception.DuplicateResourceException;
import com.peoplebase.api.common.exception.ResourceNotFoundException;
import com.peoplebase.api.common.util.TextNormalizer;
import com.peoplebase.api.organization.dto.DepartmentCreateRequest;
import com.peoplebase.api.organization.dto.DepartmentResponse;
import com.peoplebase.api.organization.dto.DepartmentUpdateRequest;
import com.peoplebase.api.organization.entity.Department;
import com.peoplebase.api.organization.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll(Boolean active) {
        List<Department> departments = active == null
                ? departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                : departmentRepository.findAllByActiveOrderByNameAsc(active);
        return departments.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    public DepartmentResponse create(DepartmentCreateRequest request) {
        Department department = new Department();
        apply(department, request.code(), request.name(), request.description(), null);
        department.setActive(true);
        return toResponse(departmentRepository.save(department));
    }

    public DepartmentResponse update(Long id, DepartmentUpdateRequest request) {
        Department department = getEntity(id);
        apply(department, request.code(), request.name(), request.description(), id);
        return toResponse(department);
    }

    public DepartmentResponse updateStatus(Long id, boolean active) {
        Department department = getEntity(id);
        department.setActive(active);
        return toResponse(department);
    }

    private void apply(Department department, String rawCode, String rawName, String description, Long id) {
        String code = TextNormalizer.code(rawCode);
        String name = TextNormalizer.required(rawName);
        ensureUnique(code, name, id);
        department.setCode(code);
        department.setName(name);
        department.setDescription(TextNormalizer.nullable(description));
    }

    private void ensureUnique(String code, String name, Long id) {
        boolean duplicateCode = id == null
                ? departmentRepository.existsByCodeIgnoreCase(code)
                : departmentRepository.existsByCodeIgnoreCaseAndIdNot(code, id);
        if (duplicateCode) {
            throw new DuplicateResourceException("Mã phòng ban đã tồn tại: " + code);
        }
        boolean duplicateName = id == null
                ? departmentRepository.existsByNameIgnoreCase(name)
                : departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id);
        if (duplicateName) {
            throw new DuplicateResourceException("Tên phòng ban đã tồn tại: " + name);
        }
    }

    private Department getEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban id=" + id));
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName(),
                department.getDescription(),
                department.isActive(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
