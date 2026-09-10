package com.peoplebase.api.auth.service;

import com.peoplebase.api.auth.dto.AccountEmployeeAssignmentRequest;
import com.peoplebase.api.auth.dto.AccountResponse;
import com.peoplebase.api.auth.dto.AccountStatusUpdateRequest;
import com.peoplebase.api.auth.dto.ChangeRoleRequest;
import com.peoplebase.api.auth.entity.UserAccount;
import com.peoplebase.api.auth.enums.AccountStatus;
import com.peoplebase.api.auth.enums.Role;
import com.peoplebase.api.auth.repository.UserAccountRepository;
import com.peoplebase.api.common.exception.BusinessRuleException;
import com.peoplebase.api.common.exception.DuplicateResourceException;
import com.peoplebase.api.common.exception.ResourceNotFoundException;
import com.peoplebase.api.common.exception.UnauthorizedException;
import com.peoplebase.api.common.util.TextNormalizer;
import com.peoplebase.api.employee.entity.Employee;
import com.peoplebase.api.employee.repository.EmployeeRepository;
import com.peoplebase.api.employee.service.EmployeeAccountCredentials;
import com.peoplebase.api.employee.service.EmployeeAccountProvisioning;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService, EmployeeAccountProvisioning {

    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;

    @Transactional(readOnly = true)
    public List<AccountResponse> getAll() {
        return userAccountRepository.findAll(Sort.by(Sort.Direction.ASC, "username"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Override
    public EmployeeAccountCredentials createForEmployee(Employee employee) {
        if (userAccountRepository.existsByEmployeeId(employee.getId())) {
            throw new DuplicateResourceException("Nhân viên này đã có tài khoản hệ thống.");
        }
        String username = TextNormalizer.username(employee.getCompanyEmail());
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Username đã tồn tại: " + username);
        }

        String temporaryPassword = temporaryPasswordGenerator.generate();
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        account.setRole(Role.EMPLOYEE);
        account.setStatus(AccountStatus.ACTIVE);
        account.setEmployee(employee);
        userAccountRepository.save(account);
        return new EmployeeAccountCredentials(username, temporaryPassword);
    }

    public void createBootstrapAdmin(String rawUsername, String password) {
        if (userAccountRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        String username = TextNormalizer.username(rawUsername);
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Username bootstrap đã tồn tại: " + username);
        }
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(Role.ADMIN);
        account.setStatus(AccountStatus.ACTIVE);
        account.setEmployee(null);
        userAccountRepository.save(account);
    }

    public AccountResponse changeRole(Long id, ChangeRoleRequest request) {
        UserAccount account = getEntity(id);
        validateRoleEmployeeLink(request.role(), account.getEmployee());
        ensureActiveAdminRemains(account, request.role(), account.getStatus());
        account.setRole(request.role());
        return toResponse(account);
    }

    public AccountResponse updateStatus(Long id, AccountStatusUpdateRequest request, String currentUsername) {
        UserAccount account = getEntity(id);
        if (!request.status().equals(AccountStatus.ACTIVE) && account.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new BusinessRuleException("Không thể tự khóa hoặc vô hiệu hóa tài khoản đang đăng nhập.");
        }
        ensureActiveAdminRemains(account, account.getRole(), request.status());
        account.setStatus(request.status());
        return toResponse(account);
    }

    public AccountResponse assignEmployee(Long id, AccountEmployeeAssignmentRequest request) {
        UserAccount account = getEntity(id);
        Employee employee = resolveEmployeeForAccount(request.employeeId(), account.getId());
        validateRoleEmployeeLink(account.getRole(), employee);
        account.setEmployee(employee);
        return toResponse(account);
    }

    public UserAccount authenticate(String rawUsername, String password) {
        UserAccount account = userAccountRepository.findByUsernameIgnoreCase(TextNormalizer.username(rawUsername))
                .orElseThrow(() -> new UnauthorizedException("Tên đăng nhập, mật khẩu không đúng hoặc tài khoản không hoạt động."));
        if (account.getStatus() != AccountStatus.ACTIVE || !passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new UnauthorizedException("Tên đăng nhập, mật khẩu không đúng hoặc tài khoản không hoạt động.");
        }
        account.setLastLoginAt(LocalDateTime.now());
        return account;
    }

    @Transactional(readOnly = true)
    public AccountResponse getCurrentAccount(String username) {
        return toResponse(getEntityByUsername(username));
    }

    @Transactional(readOnly = true)
    @Override
    public Long requireEmployeeId(String username) {
        Employee employee = getEntityByUsername(username).getEmployee();
        if (employee == null) {
            throw new BusinessRuleException("Tài khoản này không liên kết với hồ sơ nhân viên.");
        }
        return employee.getId();
    }

    @Override
    public void disableForEmployee(Long employeeId) {
        userAccountRepository.findByEmployeeId(employeeId)
                .ifPresent(account -> account.setStatus(AccountStatus.DISABLED));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByUsernameIgnoreCase(TextNormalizer.username(username))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản."));
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + account.getRole().name());
        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .authorities(authority)
                .disabled(account.getStatus() != AccountStatus.ACTIVE)
                .build();
    }

    private UserAccount getEntity(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản id=" + id));
    }

    private UserAccount getEntityByUsername(String username) {
        return userAccountRepository.findByUsernameIgnoreCase(TextNormalizer.username(username))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản."));
    }

    private Employee resolveEmployeeForAccount(Long employeeId, Long accountId) {
        if (employeeId == null) {
            return null;
        }
        UserAccount existingAccount = userAccountRepository.findByEmployeeId(employeeId).orElse(null);
        if (existingAccount != null && !existingAccount.getId().equals(accountId)) {
            throw new DuplicateResourceException("Nhân viên này đã liên kết với một tài khoản khác.");
        }
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + employeeId));
    }

    private void validateRoleEmployeeLink(Role role, Employee employee) {
        if (role != Role.ADMIN && employee == null) {
            throw new BusinessRuleException("Account có role " + role + " bắt buộc phải liên kết với Employee.");
        }
    }

    private void ensureActiveAdminRemains(UserAccount account, Role targetRole, AccountStatus targetStatus) {
        boolean removesLastActiveAdmin = account.getRole() == Role.ADMIN
                && account.getStatus() == AccountStatus.ACTIVE
                && (targetRole != Role.ADMIN || targetStatus != AccountStatus.ACTIVE)
                && countActiveAdmins() == 1;
        if (removesLastActiveAdmin) {
            throw new BusinessRuleException("Hệ thống phải luôn có ít nhất một ADMIN đang hoạt động.");
        }
    }

    private long countActiveAdmins() {
        return userAccountRepository.findAll().stream()
                .filter(account -> account.getRole() == Role.ADMIN)
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .count();
    }

    private AccountResponse toResponse(UserAccount account) {
        Employee employee = account.getEmployee();
        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.getRole(),
                account.getStatus(),
                employee == null ? null : employee.getId(),
                employee == null ? null : employee.getEmployeeCode(),
                employee == null ? null : employee.getFullName(),
                account.getLastLoginAt(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
