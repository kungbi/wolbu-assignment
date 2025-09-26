package com.company.wolbu.assignment.health.controller;

import com.company.wolbu.assignment.dto.ApiResponse;
import com.company.wolbu.assignment.health.dto.HealthStatusDto;
import com.company.wolbu.assignment.health.service.HealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check API 컨트롤러
 * 애플리케이션의 상태를 확인하는 엔드포인트를 제공합니다.
 * 
 * <p>이 컨트롤러는 다음과 같은 기능을 제공합니다:</p>
 * <ul>
 *   <li>애플리케이션 기본 상태 확인</li>
 *   <li>애플리케이션 생존 상태 확인 (Liveness Probe)</li>
 *   <li>애플리케이션 준비 상태 확인 (Readiness Probe)</li>
 * </ul>
 * 
 * <p>모든 응답은 표준 ApiResponse 형식을 따릅니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {
    
    private final HealthService healthService;
    
    /**
     * 애플리케이션의 기본 상태를 확인합니다.
     * 
     * <p>이 엔드포인트는 애플리케이션의 전반적인 상태를 확인하며,
     * 모니터링 시스템에서 주기적으로 호출할 수 있습니다.</p>
     * 
     * @return 애플리케이션 상태 정보가 포함된 표준 응답
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthStatusDto>> health() {
        log.info("Health check 요청 수신");
        
        try {
            HealthStatusDto healthStatus = healthService.checkApplicationStatus();
            ApiResponse<HealthStatusDto> response = ApiResponse.success(healthStatus);
            
            log.info("Health check 완료: status={}", healthStatus.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Health check 중 오류 발생", e);
            
            ApiResponse<HealthStatusDto> errorResponse = ApiResponse.failure(
                    "HEALTH_CHECK_ERROR", 
                    "Health check 중 오류가 발생했습니다: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 애플리케이션의 생존 상태를 확인합니다.
     * 
     * <p>이 엔드포인트는 Kubernetes Liveness Probe에서 사용되며,
     * 애플리케이션이 살아있는지 간단히 확인합니다.</p>
     * 
     * @return 애플리케이션 생존 상태 정보가 포함된 표준 응답
     */
    @GetMapping("/health/liveness")
    public ResponseEntity<ApiResponse<HealthStatusDto>> liveness() {
        log.debug("Liveness check 요청 수신");
        
        try {
            HealthStatusDto healthStatus = healthService.checkLiveness();
            ApiResponse<HealthStatusDto> response = ApiResponse.success(healthStatus);
            
            log.debug("Liveness check 완료: status={}", healthStatus.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Liveness check 중 오류 발생", e);
            
            ApiResponse<HealthStatusDto> errorResponse = ApiResponse.failure(
                    "LIVENESS_CHECK_ERROR", 
                    "Liveness check 중 오류가 발생했습니다: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 애플리케이션의 준비 상태를 확인합니다.
     * 
     * <p>이 엔드포인트는 Kubernetes Readiness Probe에서 사용되며,
     * 애플리케이션이 요청을 처리할 준비가 되었는지 확인합니다.</p>
     * 
     * @return 애플리케이션 준비 상태 정보가 포함된 표준 응답
     */
    @GetMapping("/health/readiness")
    public ResponseEntity<ApiResponse<HealthStatusDto>> readiness() {
        log.debug("Readiness check 요청 수신");
        
        try {
            HealthStatusDto healthStatus = healthService.checkReadiness();
            ApiResponse<HealthStatusDto> response = ApiResponse.success(healthStatus);
            
            log.debug("Readiness check 완료: status={}", healthStatus.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Readiness check 중 오류 발생", e);
            
            ApiResponse<HealthStatusDto> errorResponse = ApiResponse.failure(
                    "READINESS_CHECK_ERROR", 
                    "Readiness check 중 오류가 발생했습니다: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
