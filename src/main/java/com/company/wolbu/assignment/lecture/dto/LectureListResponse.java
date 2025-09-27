package com.company.wolbu.assignment.lecture.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import lombok.Getter;

/**
 * 강의 목록 조회 응답 DTO
 */
@Getter
public class LectureListResponse {
    
    private final Long id;
    private final String title;
    private final Integer price;
    private final String instructorName;
    private final Long currentEnrollmentCount;
    private final Integer maxCapacity;
    private final BigDecimal enrollmentRate;
    private final LocalDateTime createdAt;

    public LectureListResponse(Long id, String title, Integer price, String instructorName, 
                              Long currentEnrollmentCount, Integer maxCapacity, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.instructorName = instructorName;
        this.currentEnrollmentCount = currentEnrollmentCount;
        this.maxCapacity = maxCapacity;
        this.createdAt = createdAt;
        
        // 신청률 계산 (신청자 수 ÷ 최대 정원)
        if (maxCapacity > 0) {
            this.enrollmentRate = BigDecimal.valueOf(currentEnrollmentCount)
                    .divide(BigDecimal.valueOf(maxCapacity), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)); // 백분율로 변환
        } else {
            this.enrollmentRate = BigDecimal.ZERO;
        }
    }
}
