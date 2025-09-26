package com.company.wolbu.assignment.health.service;

import com.company.wolbu.assignment.health.dto.HealthStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Health Check 서비스
 * 애플리케이션의 상태를 확인하고 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthService {
    
    @Value("${spring.application.name:assignment}")
    private String applicationName;
    
    @Value("${spring.application.version:0.0.1-SNAPSHOT}")
    private String applicationVersion;
    
    /**
     * 애플리케이션의 현재 상태를 확인합니다.
     * 
     * @return 애플리케이션 상태 정보
     */
    public HealthStatusDto checkApplicationStatus() {
        log.debug("애플리케이션 상태 확인 요청");
        
        try {
            // 실제 애플리케이션 상태 확인 로직
            // 현재는 단순히 UP 상태를 반환하지만, 
            // 향후 DB 연결, 외부 서비스 연동 등을 확인할 수 있습니다.
            HealthStatusDto status = HealthStatusDto.builder()
                    .status("UP")
                    .application(applicationName)
                    .timestamp(LocalDateTime.now())
                    .version(applicationVersion)
                    .message("Application is running normally")
                    .build();
            
            log.debug("애플리케이션 상태 확인 완료: {}", status.getStatus());
            return status;
            
        } catch (Exception e) {
            log.error("애플리케이션 상태 확인 중 오류 발생", e);
            
            // 오류 발생 시 DOWN 상태 반환
            return HealthStatusDto.builder()
                    .status("DOWN")
                    .application(applicationName)
                    .timestamp(LocalDateTime.now())
                    .version(applicationVersion)
                    .message("Application is experiencing issues: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 애플리케이션이 살아있는지 확인합니다.
     * 
     * @return 애플리케이션 생존 상태
     */
    public HealthStatusDto checkLiveness() {
        log.debug("애플리케이션 생존 상태 확인 요청");
        
        return HealthStatusDto.builder()
                .status("UP")
                .application(applicationName)
                .timestamp(LocalDateTime.now())
                .version(applicationVersion)
                .message("Application is alive")
                .build();
    }
    
    /**
     * 애플리케이션이 요청을 처리할 준비가 되었는지 확인합니다.
     * 
     * @return 애플리케이션 준비 상태
     */
    public HealthStatusDto checkReadiness() {
        log.debug("애플리케이션 준비 상태 확인 요청");
        
        try {
            // 실제 준비 상태 확인 로직
            // 예: DB 연결 확인, 캐시 연결 확인 등
            HealthStatusDto status = HealthStatusDto.builder()
                    .status("UP")
                    .application(applicationName)
                    .timestamp(LocalDateTime.now())
                    .version(applicationVersion)
                    .message("Application is ready to serve requests")
                    .build();
            
            log.debug("애플리케이션 준비 상태 확인 완료: {}", status.getStatus());
            return status;
            
        } catch (Exception e) {
            log.error("애플리케이션 준비 상태 확인 중 오류 발생", e);
            
            return HealthStatusDto.builder()
                    .status("DOWN")
                    .application(applicationName)
                    .timestamp(LocalDateTime.now())
                    .version(applicationVersion)
                    .message("Application is not ready: " + e.getMessage())
                    .build();
        }
    }
}
