package com.company.wolbu.assignment.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.company.wolbu.assignment.common.dto.ApiResponseDto;

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
    public ResponseEntity<ApiResponseDto<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.httpStatus())
                .body(ApiResponseDto.failure(e.errorCode(), e.getMessage()));
    }


    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponseDto<Void>> handleValidation(Exception e) {
        String message = "입력값이 올바르지 않습니다.";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            if (ex.getBindingResult().hasFieldErrors()) {
                String defaultMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
                message = defaultMessage != null ? defaultMessage : message;
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.failure("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMissingRequestHeader(MissingRequestHeaderException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.failure("MISSING_HEADER", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        // Unique 제약 위반 (중복 신청) 처리
        if (e.getMessage() != null && e.getMessage().contains("uk_enrollment_lecture_member")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponseDto.failure("DUPLICATE_ENROLLMENT", "이미 신청한 강의입니다."));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.failure("DATA_INTEGRITY_ERROR", "데이터 무결성 오류가 발생했습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.failure("INVALID_INPUT", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleOthers(Exception e) {
        // 로깅 추가 (실제 운영에서는 로그 레벨 조정)
        System.err.println("Unexpected error: " + e.getMessage());
        e.printStackTrace();
        
        // JWT 관련 오류인 경우 더 친절한 메시지
        if (e.getMessage() != null && e.getMessage().contains("JWT")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.failure("AUTH_ERROR", "인증 처리 중 오류가 발생했습니다."));
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.failure("INTERNAL_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
