package com.company.wolbu.assignment.auth.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.company.wolbu.assignment.auth.domain.MemberRole;

/**
 * 특정 역할이 필요한 메서드에 사용하는 어노테이션
 * 
 * 사용 예시:
 * @RequireRole(MemberRole.INSTRUCTOR) - 강사만 접근 가능
 * @RequireRole(MemberRole.STUDENT) - 수강생만 접근 가능
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * 필요한 역할
     */
    MemberRole value();
    
    /**
     * 에러 메시지 (선택사항)
     */
    String message() default "";
}
