package com.company.wolbu.assignment.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.company.wolbu.assignment.common.dto.ApiResponse;

/**
 * 전역 예외 처리기
 * 애플리케이션 전반에서 발생하는 예외를 일관된 형식으로 처리합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 비즈니스 예외 처리
     * 각 예외에서 정의한 HTTP 상태 코드와 에러 코드를 사용합니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.httpStatus())
                .body(ApiResponse.failure(e.errorCode(), e.getMessage()));
    }


    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception e) {
        String message = "입력값이 올바르지 않습니다.";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            if (ex.getBindingResult().hasFieldErrors()) {
                String field = ex.getBindingResult().getFieldErrors().get(0).getField();
                String defaultMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
                
                if ("email".equals(field)) {
                    message = "이메일 형식이 올바르지 않습니다.";
                } else if ("password".equals(field)) {
                    message = "비밀번호 형식이 올바르지 않습니다.";
                } else if ("name".equals(field)) {
                    message = "이름을 입력해주세요.";
                } else if ("phone".equals(field)) {
                    message = "휴대폰 번호를 입력해주세요.";
                } else if ("role".equals(field)) {
                    message = "회원 유형을 선택해주세요.";
                } else if ("lectureIds".equals(field)) {
                    message = "신청할 강의를 선택해주세요.";
                } else {
                    message = defaultMessage != null ? defaultMessage : message;
                }
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeader(MissingRequestHeaderException e) {
        String headerName = e.getHeaderName();
        if ("Authorization".equals(headerName)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("MISSING_AUTH_HEADER", "인증 토큰이 필요합니다."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("MISSING_HEADER", "필수 헤더가 누락되었습니다: " + headerName));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        // Unique 제약 위반 (중복 신청) 처리
        if (e.getMessage() != null && e.getMessage().contains("uk_enrollment_lecture_member")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.failure("DUPLICATE_ENROLLMENT", "이미 신청한 강의입니다."));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("DATA_INTEGRITY_ERROR", "데이터 무결성 오류가 발생했습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("INVALID_INPUT", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOthers(Exception e) {
        // 로깅 추가 (실제 운영에서는 로그 레벨 조정)
        System.err.println("Unexpected error: " + e.getMessage());
        e.printStackTrace();
        
        // JWT 관련 오류인 경우 더 친절한 메시지
        if (e.getMessage() != null && e.getMessage().contains("JWT")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("AUTH_ERROR", "인증 처리 중 오류가 발생했습니다."));
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("INTERNAL_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
