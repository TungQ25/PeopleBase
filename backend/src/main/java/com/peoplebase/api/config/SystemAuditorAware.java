package com.peoplebase.api.config;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class SystemAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // TODO Replace with the authenticated username when JWT security is implemented.
        return Optional.of("system");
    }
}
