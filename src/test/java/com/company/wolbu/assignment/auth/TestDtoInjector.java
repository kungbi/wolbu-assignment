package com.company.wolbu.assignment.auth;

import java.lang.reflect.Field;

final class TestDtoInjector {
    private TestDtoInjector() {}

    static void set(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


