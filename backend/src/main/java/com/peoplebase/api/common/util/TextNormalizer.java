package com.peoplebase.api.common.util;

import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String required(String value) {
        return value.trim();
    }

    public static String nullable(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    public static String code(String value) {
        return required(value).toUpperCase(Locale.ROOT);
    }

    public static String email(String value) {
        return required(value).toLowerCase(Locale.ROOT);
    }

    public static String username(String value) {
        return required(value).toLowerCase(Locale.ROOT);
    }
}
