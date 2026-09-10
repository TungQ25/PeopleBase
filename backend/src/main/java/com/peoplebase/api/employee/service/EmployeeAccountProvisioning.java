package com.peoplebase.api.employee.service;

import com.peoplebase.api.employee.entity.Employee;

public interface EmployeeAccountProvisioning {

    EmployeeAccountCredentials createForEmployee(Employee employee);

    void disableForEmployee(Long employeeId);

    Long requireEmployeeId(String username);
}
