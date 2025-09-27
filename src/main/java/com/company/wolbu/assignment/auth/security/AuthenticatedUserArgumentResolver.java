package com.company.wolbu.assignment.auth.security;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.common.exception.DomainException;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 토큰에서 사용자 정보를 추출하여 AuthenticatedUser 객체로 주입하는 ArgumentResolver
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtProvider jwtProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(AuthenticatedUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        
        String authorization = webRequest.getHeader("Authorization");
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new DomainException("INVALID_TOKEN", "유효하지 않은 인증 토큰입니다.");
        }
        
        String token = authorization.substring(7); // "Bearer " 제거
        
        try {
            Claims claims = jwtProvider.parse(token);
            Long memberId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            String roleString = claims.get("role", String.class);
            MemberRole role = MemberRole.valueOf(roleString);
            
            return new AuthenticatedUser(memberId, email, role);
            
        } catch (Exception e) {
            log.warn("JWT 토큰 파싱 실패: {}", e.getMessage());
            throw new DomainException("INVALID_TOKEN", "유효하지 않은 인증 토큰입니다.");
        }
    }
}
