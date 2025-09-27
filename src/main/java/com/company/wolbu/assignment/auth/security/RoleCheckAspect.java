package com.company.wolbu.assignment.auth.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.common.exception.DomainException;

import lombok.extern.slf4j.Slf4j;

/**
 * @RequireRole 어노테이션을 처리하는 AOP Aspect
 */
@Slf4j
@Aspect
@Component
public class RoleCheckAspect {

    /**
     * @RequireRole 어노테이션이 붙은 메서드 실행 전에 권한을 확인합니다.
     */
    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        // 메서드 파라미터에서 AuthenticatedUser 찾기
        Object[] args = joinPoint.getArgs();
        AuthenticatedUser authenticatedUser = null;
        
        for (Object arg : args) {
            if (arg instanceof AuthenticatedUser) {
                authenticatedUser = (AuthenticatedUser) arg;
                break;
            }
        }
        
        if (authenticatedUser == null) {
            log.error("@RequireRole 어노테이션을 사용하려면 메서드 파라미터에 AuthenticatedUser가 있어야 합니다.");
            throw new DomainException("INTERNAL_ERROR", "권한 검증 설정 오류입니다.");
        }
        
        MemberRole requiredRole = requireRole.value();
        if (!authenticatedUser.hasRole(requiredRole)) {
            String message = requireRole.message().isEmpty() 
                ? String.format("%s 권한이 필요합니다.", getRoleDisplayName(requiredRole))
                : requireRole.message();
            
            log.warn("권한 부족: 사용자 역할={}, 필요 역할={}, 사용자ID={}", 
                authenticatedUser.getRole(), requiredRole, authenticatedUser.getMemberId());
            
            throw new DomainException("INSUFFICIENT_ROLE", message);
        }
        
        log.debug("권한 검증 통과: 사용자 역할={}, 필요 역할={}", 
            authenticatedUser.getRole(), requiredRole);
    }
    
    /**
     * 역할의 표시 이름을 반환합니다.
     */
    private String getRoleDisplayName(MemberRole role) {
        switch (role) {
            case INSTRUCTOR:
                return "강사";
            case STUDENT:
                return "수강생";
            default:
                return role.name();
        }
    }
}
