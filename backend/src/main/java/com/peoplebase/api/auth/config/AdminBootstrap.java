package com.peoplebase.api.auth.config;

import com.peoplebase.api.auth.enums.Role;
import com.peoplebase.api.auth.repository.UserAccountRepository;
import com.peoplebase.api.auth.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminBootstrapProperties properties;
    private final UserAccountRepository userAccountRepository;
    private final AccountService accountService;

    @Override
    public void run(ApplicationArguments args) {
        if (userAccountRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        if (!StringUtils.hasText(properties.username()) && !StringUtils.hasText(properties.password())) {
            logger.warn("Chưa cấu hình APP_ADMIN_USERNAME và APP_ADMIN_PASSWORD; không tạo ADMIN bootstrap.");
            return;
        }
        if (!StringUtils.hasText(properties.username()) || !StringUtils.hasText(properties.password())) {
            throw new IllegalStateException("Cần cấu hình đồng thời APP_ADMIN_USERNAME và APP_ADMIN_PASSWORD.");
        }
        accountService.createBootstrapAdmin(properties.username(), properties.password());
        logger.info("Đã tạo ADMIN bootstrap từ cấu hình môi trường.");
    }
}
