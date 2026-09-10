package com.peoplebase.api.auth.repository;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmployeeId(Long employeeId);

    Optional<UserAccount> findByEmployeeId(Long employeeId);

    boolean existsByRole(Role role);
}
