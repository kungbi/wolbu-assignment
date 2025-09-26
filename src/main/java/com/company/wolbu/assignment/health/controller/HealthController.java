package com.company.wolbu.assignment.health.controller;

import com.company.wolbu.assignment.dto.ApiResponse;
import com.company.wolbu.assignment.health.dto.HealthStatusDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.application.name:assignment}")
    private String applicationName;

    @Value("${spring.application.version:0.0.1-SNAPSHOT}")
    private String applicationVersion;

    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthStatusDto>> health() {
        HealthStatusDto healthStatus = HealthStatusDto.builder()
                .status("UP")
                .application(applicationName)
                .timestamp(LocalDateTime.now())
                .version(applicationVersion)
                .message("Application is running normally")
                .build();
        return ResponseEntity.ok(ApiResponse.success(healthStatus));
    }
    
}
