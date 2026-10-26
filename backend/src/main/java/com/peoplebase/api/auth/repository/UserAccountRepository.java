package com.peoplebase.api.auth.repository;

import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    @EntityGraph(attributePaths = "accountRoles")
    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    @Override
    @EntityGraph(attributePaths = "accountRoles")
    Optional<UserAccount> findById(Long id);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmployeeId(Long employeeId);

    @EntityGraph(attributePaths = "accountRoles")
    Optional<UserAccount> findByEmployeeId(Long employeeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from UserAccount account where account.employee.id = :employeeId")
    Optional<UserAccount> findByEmployeeIdForUpdate(@Param("employeeId") Long employeeId);

    @Query("select (count(account) > 0) from UserAccount account join account.accountRoles grant "
            + "where grant.role = :role and grant.validFrom <= local datetime "
            + "and (grant.validTo is null or grant.validTo > local datetime)")
    boolean existsByRole(@Param("role") Role role);
}
