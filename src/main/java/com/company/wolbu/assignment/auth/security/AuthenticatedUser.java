package com.company.wolbu.assignment.auth.security;

import com.company.wolbu.assignment.auth.domain.MemberRole;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인증된 사용자 정보를 담는 클래스
 */
@Getter
@RequiredArgsConstructor
public class AuthenticatedUser {
    
    private final Long memberId;
    private final String email;
    private final MemberRole role;
    
    /**
     * 강사인지 확인
     */
    public boolean isInstructor() {
        return role == MemberRole.INSTRUCTOR;
    }
    
    /**
     * 수강생인지 확인
     */
    public boolean isStudent() {
        return role == MemberRole.STUDENT;
    }
    
    /**
     * 특정 역할인지 확인
     */
    public boolean hasRole(MemberRole requiredRole) {
        return this.role == requiredRole;
    }
}
