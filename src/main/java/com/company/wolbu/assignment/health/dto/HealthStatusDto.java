package com.company.wolbu.assignment.health.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Health Check 상태 정보 DTO
 * 애플리케이션의 현재 상태를 나타냅니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatusDto {
    
    /**
     * 애플리케이션 상태 (UP, DOWN)
     */
    private String status;
    
    /**
     * 애플리케이션 이름
     */
    private String application;
    
    /**
     * 현재 시간
     */
    private LocalDateTime timestamp;
    
    /**
     * 애플리케이션 버전
     */
    private String version;
    
    /**
     * 상태 메시지
     */
    private String message;
}
