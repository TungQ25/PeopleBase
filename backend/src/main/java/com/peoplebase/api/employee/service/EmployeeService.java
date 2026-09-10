package com.peoplebase.api.employee.service;

import com.peoplebase.api.common.exception.BusinessRuleException;
import com.peoplebase.api.common.exception.DuplicateResourceException;
import com.peoplebase.api.common.exception.ResourceNotFoundException;
import com.peoplebase.api.common.util.TextNormalizer;
import com.peoplebase.api.employee.dto.EmployeeCreateRequest;
import com.peoplebase.api.employee.dto.EmployeeCreationResponse;
import com.peoplebase.api.employee.dto.EmployeeResponse;
import com.peoplebase.api.employee.dto.EmployeeStatusUpdateRequest;
import com.peoplebase.api.employee.dto.EmployeeUpdateRequest;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.employee.enums.EmployeeStatus;
import com.peoplebase.api.employee.repository.EmployeeRepository;
import com.peoplebase.api.organization.entity.Department;
import com.peoplebase.api.organization.entity.Position;
import com.peoplebase.api.organization.repository.DepartmentRepository;
import com.peoplebase.api.organization.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeAccountProvisioning employeeAccountProvisioning;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll(Long departmentId, EmployeeStatus status) {
        List<Employee> employees;
        if (departmentId != null && status != null) {
            employees = employeeRepository.findAllByDepartmentIdAndStatusOrderByFullNameAsc(departmentId, status);
        } else if (departmentId != null) {
            employees = employeeRepository.findAllByDepartmentIdOrderByFullNameAsc(departmentId);
        } else if (status != null) {
            employees = employeeRepository.findAllByStatusOrderByFullNameAsc(status);
        } else {
            employees = employeeRepository.findAll(Sort.by(Sort.Direction.ASC, "fullName"));
        }
        return employees.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    public EmployeeCreationResponse create(EmployeeCreateRequest request) {
        Employee employee = new Employee();
        apply(
                employee,
                request.employeeCode(), request.firstName(), request.lastName(), request.dateOfBirth(), request.gender(),
                request.phone(), request.personalEmail(), request.companyEmail(), request.address(), request.hireDate(),
                request.departmentId(), request.positionId(), request.managerId(), null
        );
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setTerminationDate(null);

        Employee savedEmployee = employeeRepository.save(employee);
        EmployeeAccountCredentials credentials = employeeAccountProvisioning.createForEmployee(savedEmployee);
        return new EmployeeCreationResponse(toResponse(savedEmployee), credentials.username(), credentials.temporaryPassword());
    }

    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        Employee employee = getEntity(id);
        apply(
                employee,
                request.employeeCode(), request.firstName(), request.lastName(), request.dateOfBirth(), request.gender(),
                request.phone(), request.personalEmail(), request.companyEmail(), request.address(), request.hireDate(),
                request.departmentId(), request.positionId(), request.managerId(), id
        );
        if (employee.getTerminationDate() != null && employee.getHireDate().isAfter(employee.getTerminationDate())) {
            throw new BusinessRuleException("Ngày tuyển dụng không được sau ngày nghỉ việc.");
        }
        return toResponse(employee);
    }

    public EmployeeResponse updateStatus(Long id, EmployeeStatusUpdateRequest request) {
        Employee employee = getEntity(id);
        if (request.status() != EmployeeStatus.RESIGNED) {
            employee.setTerminationDate(null);
        } else {
            if (request.terminationDate() == null) {
                throw new BusinessRuleException("Cần cung cấp ngày nghỉ việc khi nhân viên đã nghỉ việc.");
            }
            if (request.terminationDate().isBefore(employee.getHireDate())) {
                throw new BusinessRuleException("Ngày nghỉ việc không được trước ngày tuyển dụng.");
            }
            employee.setTerminationDate(request.terminationDate());
            employeeAccountProvisioning.disableForEmployee(id);
        }
        employee.setStatus(request.status());
        return toResponse(employee);
    }

    private void apply(
            Employee employee,
            String rawEmployeeCode,
            String rawFirstName,
            String rawLastName,
            LocalDate dateOfBirth,
            com.peoplebase.api.employee.enums.Gender gender,
            String phone,
            String personalEmail,
            String rawCompanyEmail,
            String address,
            LocalDate hireDate,
            Long departmentId,
            Long positionId,
            Long managerId,
            Long existingEmployeeId
    ) {
        String employeeCode = TextNormalizer.code(rawEmployeeCode);
        String firstName = TextNormalizer.required(rawFirstName);
        String lastName = TextNormalizer.required(rawLastName);
        String companyEmail = TextNormalizer.email(rawCompanyEmail);
        ensureUnique(employeeCode, companyEmail, existingEmployeeId);
        validateDates(dateOfBirth, hireDate);

        employee.setEmployeeCode(employeeCode);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setFullName(firstName + " " + lastName);
        employee.setDateOfBirth(dateOfBirth);
        employee.setGender(gender);
        employee.setPhone(TextNormalizer.nullable(phone));
        employee.setPersonalEmail(personalEmail == null ? null : TextNormalizer.email(personalEmail));
        employee.setCompanyEmail(companyEmail);
        employee.setAddress(TextNormalizer.nullable(address));
        employee.setHireDate(hireDate);
        employee.setDepartment(resolveDepartment(departmentId));
        employee.setPosition(resolvePosition(positionId));
        employee.setManager(resolveManager(managerId, existingEmployeeId));
    }

    private Department resolveDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban id=" + id));
        if (!department.isActive()) {
            throw new BusinessRuleException("Không thể gán nhân viên vào phòng ban đã ngừng hoạt động.");
        }
        return department;
    }

    private Position resolvePosition(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức danh id=" + id));
        if (!position.isActive()) {
            throw new BusinessRuleException("Không thể gán nhân viên vào chức danh đã ngừng hoạt động.");
        }
        return position;
    }

    private Employee resolveManager(Long managerId, Long employeeId) {
        if (managerId == null) {
            return null;
        }
        if (managerId.equals(employeeId)) {
            throw new BusinessRuleException("Nhân viên không thể là quản lý trực tiếp của chính mình.");
        }
        Employee manager = getEntity(managerId);
        if (employeeId != null) {
            validateManagerHierarchy(manager, employeeId);
        }
        return manager;
    }

    private void validateManagerHierarchy(Employee manager, Long employeeId) {
        Set<Long> visited = new HashSet<>();
        Employee cursor = manager;
        while (cursor != null) {
            if (employeeId.equals(cursor.getId())) {
                throw new BusinessRuleException("Không thể tạo vòng lặp trong cấu trúc quản lý.");
            }
            if (!visited.add(cursor.getId())) {
                throw new BusinessRuleException("Cấu trúc quản lý hiện tại có vòng lặp không hợp lệ.");
            }
            cursor = cursor.getManager();
        }
    }

    private void validateDates(LocalDate dateOfBirth, LocalDate hireDate) {
        if (dateOfBirth != null && !dateOfBirth.isBefore(hireDate)) {
            throw new BusinessRuleException("Ngày sinh phải trước ngày tuyển dụng.");
        }
    }

    private void ensureUnique(String employeeCode, String companyEmail, Long id) {
        boolean duplicateCode = id == null
                ? employeeRepository.existsByEmployeeCodeIgnoreCase(employeeCode)
                : employeeRepository.existsByEmployeeCodeIgnoreCaseAndIdNot(employeeCode, id);
        if (duplicateCode) {
            throw new DuplicateResourceException("Mã nhân viên đã tồn tại: " + employeeCode);
        }
        boolean duplicateCompanyEmail = id == null
                ? employeeRepository.existsByCompanyEmailIgnoreCase(companyEmail)
                : employeeRepository.existsByCompanyEmailIgnoreCaseAndIdNot(companyEmail, id);
        if (duplicateCompanyEmail) {
            throw new DuplicateResourceException("Email công ty đã tồn tại: " + companyEmail);
        }
    }

    private Employee getEntity(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + id));
    }

    private EmployeeResponse toResponse(Employee employee) {
        Department department = employee.getDepartment();
        Position position = employee.getPosition();
        Employee manager = employee.getManager();
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getFullName(),
                employee.getDateOfBirth(),
                employee.getGender(),
                employee.getPhone(),
                employee.getPersonalEmail(),
                employee.getCompanyEmail(),
                employee.getAddress(),
                employee.getHireDate(),
                employee.getTerminationDate(),
                employee.getStatus(),
                department.getId(),
                department.getCode(),
                department.getName(),
                position.getId(),
                position.getCode(),
                position.getName(),
                manager == null ? null : manager.getId(),
                manager == null ? null : manager.getEmployeeCode(),
                manager == null ? null : manager.getFullName(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
