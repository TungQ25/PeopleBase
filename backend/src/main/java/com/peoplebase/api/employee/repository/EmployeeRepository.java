package com.peoplebase.api.employee.repository;

import com.peoplebase.api.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmailIgnoreCase(String email);
}
