package com.peoplebase.api.employee.repository;

import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.employee.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCodeIgnoreCase(String employeeCode);

    Optional<Employee> findByCompanyEmailIgnoreCase(String companyEmail);

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByCompanyEmailIgnoreCase(String companyEmail);

    boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String employeeCode, Long id);

    boolean existsByCompanyEmailIgnoreCaseAndIdNot(String companyEmail, Long id);

    List<Employee> findAllByDepartmentIdOrderByFullNameAsc(Long departmentId);

    List<Employee> findAllByStatusOrderByFullNameAsc(EmployeeStatus status);

    List<Employee> findAllByDepartmentIdAndStatusOrderByFullNameAsc(Long departmentId, EmployeeStatus status);

}
