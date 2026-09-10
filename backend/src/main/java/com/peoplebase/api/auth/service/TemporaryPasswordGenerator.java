package com.peoplebase.api.auth.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TemporaryPasswordGenerator {

    private static final char[] PASSWORD_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%".toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        char[] password = new char[16];
        for (int index = 0; index < password.length; index++) {
            password[index] = PASSWORD_CHARACTERS[secureRandom.nextInt(PASSWORD_CHARACTERS.length)];
        }
        return new String(password);
    }
}
