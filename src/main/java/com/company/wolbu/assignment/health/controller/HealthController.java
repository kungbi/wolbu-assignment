package com.company.wolbu.assignment.health.controller;

import com.company.wolbu.assignment.common.dto.ApiResponseDto;
import com.company.wolbu.assignment.health.dto.HealthStatusDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@Tag(name = "헬스체크 API", description = "애플리케이션 상태 확인을 위한 API")
public class HealthController {

    @Value("${spring.application.name:assignment}")
    private String applicationName;

    @Value("${spring.application.version:0.0.1-SNAPSHOT}")
    private String applicationVersion;

    @Operation(
        summary = "애플리케이션 상태 확인",
        description = "애플리케이션의 현재 상태와 버전 정보를 확인합니다. 인증 없이 접근 가능합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 확인 성공")
    })
    @GetMapping("/health")
    public ResponseEntity<ApiResponseDto<HealthStatusDto>> health() {
        HealthStatusDto healthStatus = HealthStatusDto.builder()
                .status("UP")
                .application(applicationName)
                .timestamp(LocalDateTime.now())
                .version(applicationVersion)
                .message("Application is running normally")
                .build();
        return ResponseEntity.ok(ApiResponseDto.success(healthStatus));
    }
    
}
