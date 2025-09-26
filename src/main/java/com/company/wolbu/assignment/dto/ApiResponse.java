package com.company.wolbu.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 표준 API 응답 DTO
 * 모든 API 응답은 이 형식을 따릅니다.
 * 
 * @param <T> 응답 데이터 타입
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 요청 성공 여부
     */
    private boolean success;
    
    /**
     * 응답 데이터
     */
    private T data;
    
    /**
     * 에러 정보 (실패 시에만 포함)
     */
    private ErrorInfo error;
    
    /**
     * 에러 정보 내부 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInfo {
        /**
         * 에러 코드
         */
        private String code;
        
        /**
         * 에러 메시지
         */
        private String message;
    }
    
    /**
     * 성공 응답 생성
     * 
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    /**
     * 실패 응답 생성
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param <T> 데이터 타입
     * @return 실패 응답
     */
    public static <T> ApiResponse<T> failure(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }
}
