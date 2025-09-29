package com.company.wolbu.assignment.enrollment.dto;

import java.util.List;

import lombok.Getter;

/**
 * 강의 신청 결과 DTO
 */
@Getter
public class EnrollmentResultDto {
    
    private final List<EnrollmentResponseDto> successfulEnrollments;
    private final List<EnrollmentFailure> failedEnrollments;
    private final int totalRequested;
    private final int successCount;
    private final int failureCount;

    public EnrollmentResultDto(List<EnrollmentResponseDto> successfulEnrollments, 
                           List<EnrollmentFailure> failedEnrollments) {
        this.successfulEnrollments = successfulEnrollments;
        this.failedEnrollments = failedEnrollments;
        this.totalRequested = successfulEnrollments.size() + failedEnrollments.size();
        this.successCount = successfulEnrollments.size();
        this.failureCount = failedEnrollments.size();
    }

    /**
     * 강의 신청 실패 정보
     */
    @Getter
    public static class EnrollmentFailure {
        private final Long lectureId;
        private final String lectureTitle;
        private final String errorCode;
        private final String errorMessage;

        public EnrollmentFailure(Long lectureId, String lectureTitle, String errorCode, String errorMessage) {
            this.lectureId = lectureId;
            this.lectureTitle = lectureTitle;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }
    }
}
