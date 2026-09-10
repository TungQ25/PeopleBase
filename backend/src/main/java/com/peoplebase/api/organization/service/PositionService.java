package com.peoplebase.api.organization.service;

import com.peoplebase.api.common.exception.DuplicateResourceException;
import com.peoplebase.api.common.exception.ResourceNotFoundException;
import com.peoplebase.api.common.util.TextNormalizer;
import com.peoplebase.api.organization.dto.PositionCreateRequest;
import com.peoplebase.api.organization.dto.PositionResponse;
import com.peoplebase.api.organization.dto.PositionUpdateRequest;
import com.peoplebase.api.organization.entity.Position;
import com.peoplebase.api.organization.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PositionService {

    private final PositionRepository positionRepository;

    @Transactional(readOnly = true)
    public List<PositionResponse> getAll(Boolean active) {
        List<Position> positions = active == null
                ? positionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                : positionRepository.findAllByActiveOrderByNameAsc(active);
        return positions.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PositionResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    public PositionResponse create(PositionCreateRequest request) {
        Position position = new Position();
        apply(position, request.code(), request.name(), request.description(), request.level(), null);
        position.setActive(true);
        return toResponse(positionRepository.save(position));
    }

    public PositionResponse update(Long id, PositionUpdateRequest request) {
        Position position = getEntity(id);
        apply(position, request.code(), request.name(), request.description(), request.level(), id);
        return toResponse(position);
    }

    public PositionResponse updateStatus(Long id, boolean active) {
        Position position = getEntity(id);
        position.setActive(active);
        return toResponse(position);
    }

    private void apply(Position position, String rawCode, String rawName, String description, Integer level, Long id) {
        String code = TextNormalizer.code(rawCode);
        String name = TextNormalizer.required(rawName);
        ensureUnique(code, name, id);
        position.setCode(code);
        position.setName(name);
        position.setDescription(TextNormalizer.nullable(description));
        position.setLevel(level);
    }

    private void ensureUnique(String code, String name, Long id) {
        boolean duplicateCode = id == null
                ? positionRepository.existsByCodeIgnoreCase(code)
                : positionRepository.existsByCodeIgnoreCaseAndIdNot(code, id);
        if (duplicateCode) {
            throw new DuplicateResourceException("Mã chức danh đã tồn tại: " + code);
        }
        boolean duplicateName = id == null
                ? positionRepository.existsByNameIgnoreCase(name)
                : positionRepository.existsByNameIgnoreCaseAndIdNot(name, id);
        if (duplicateName) {
            throw new DuplicateResourceException("Tên chức danh đã tồn tại: " + name);
        }
    }

    private Position getEntity(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức danh id=" + id));
    }

    private PositionResponse toResponse(Position position) {
        return new PositionResponse(
                position.getId(),
                position.getCode(),
                position.getName(),
                position.getDescription(),
                position.getLevel(),
                position.isActive(),
                position.getCreatedAt(),
                position.getUpdatedAt()
        );
    }
}
