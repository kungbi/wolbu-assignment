package com.company.wolbu.assignment.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.wolbu.assignment.auth.domain.Member;
import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.domain.RefreshToken;
import com.company.wolbu.assignment.auth.dto.AuthResponseDto;
import com.company.wolbu.assignment.auth.dto.AuthResultDto;
import com.company.wolbu.assignment.auth.dto.SignUpResponseDto;
import com.company.wolbu.assignment.auth.dto.LoginRequestDto;
import com.company.wolbu.assignment.auth.dto.SignUpRequestDto;
import com.company.wolbu.assignment.auth.repository.MemberRepository;
import com.company.wolbu.assignment.auth.repository.RefreshTokenRepository;
import com.company.wolbu.assignment.auth.security.JwtProvider;
import com.company.wolbu.assignment.auth.security.PasswordPolicy;
import com.company.wolbu.assignment.auth.exception.DuplicateEmailException;
import com.company.wolbu.assignment.auth.exception.InvalidCredentialsException;
import com.company.wolbu.assignment.auth.exception.InvalidPasswordPolicyException;
import com.company.wolbu.assignment.auth.exception.TokenExpiredException;
import com.company.wolbu.assignment.enrollment.exception.MemberNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto req) {
        if (!PasswordPolicy.isValid(req.getPassword())) {
            throw new InvalidPasswordPolicyException(
                "비밀번호는 6~10자, 영문 대소문자와 숫자 중 2종 이상 조합이어야 합니다.");
        }
        if (memberRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다. 다른 이메일을 사용해주세요.");
        }

        String hash = passwordEncoder.encode(req.getPassword());
        MemberRole role = req.getRole();
        Member member = Member.create(req.getName(), req.getEmail(), req.getPhone(), hash, role);
        memberRepository.save(member);

        return new SignUpResponseDto(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }

    @Transactional
    public AuthResultDto login(LoginRequestDto req) {
        Member member = memberRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException());
        if (!passwordEncoder.matches(req.getPassword(), member.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String access = jwtProvider.generateAccessToken(member.getId(), member.getEmail(), member.getRole());
        String refresh = jwtProvider.generateRefreshToken(member.getId());

        refreshTokenRepository.deleteByMember(member);
        refreshTokenRepository.flush();
        refreshTokenRepository.save(RefreshToken.issue(member, refresh));

        return new AuthResultDto(
            new AuthResponseDto(member.getId(), member.getName(), member.getEmail(), access, member.getRole()),
            refresh
        );
    }

    @Transactional
    public AuthResultDto refreshToken(String refreshToken) {
        // 1. refresh token이 DB에 존재하는지 확인
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenExpiredException("유효하지 않은 리프레시 토큰입니다."));

        // 2. refresh token 파싱하여 사용자 ID 확인
        try {
            var claims = jwtProvider.parse(refreshToken);
            Long userId = Long.parseLong(claims.getSubject());
            
            // 3. 사용자 정보 조회
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new MemberNotFoundException(userId));

            // 4. 새로운 access token과 refresh token 생성
            String newAccess = jwtProvider.generateAccessToken(member.getId(), member.getEmail(), member.getRole());
            String newRefresh = jwtProvider.generateRefreshToken(member.getId());

            // 5. 기존 refresh token 삭제 후 새로운 토큰 저장
            refreshTokenRepository.deleteByMember(member);
            refreshTokenRepository.flush();
            refreshTokenRepository.save(RefreshToken.issue(member, newRefresh));

            return new AuthResultDto(
                new AuthResponseDto(member.getId(), member.getName(), member.getEmail(), newAccess, member.getRole()),
                newRefresh
            );
        } catch (Exception e) {
            // JWT 파싱 실패 시 refresh token 삭제
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException();
        }
    }
}