package com.company.wolbu.assignment.auth.security;

import java.util.regex.Pattern;

public final class PasswordPolicy {

    private static final Pattern RULE = Pattern.compile(
        "^(?:(?=.*[a-z])(?=.*[A-Z])|(?=.*[a-z])(?=.*[0-9])|(?=.*[A-Z])(?=.*[0-9])).{6,10}$"
    );

    private PasswordPolicy() {}

    public static boolean isValid(String raw) {
        if (raw == null) return false;
        return RULE.matcher(raw).matches();
    }
}


