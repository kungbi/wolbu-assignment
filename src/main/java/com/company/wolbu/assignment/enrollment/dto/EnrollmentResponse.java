package com.company.wolbu.assignment.enrollment.dto;

import java.time.LocalDateTime;

import lombok.Getter;

/**
 * 강의 신청 응답 DTO
 */
@Getter
public class EnrollmentResponse {
    
    private final Long enrollmentId;
    private final Long lectureId;
    private final String lectureTitle;
    private final Long memberId;
    private final String status;
    private final LocalDateTime enrolledAt;

    public EnrollmentResponse(Long enrollmentId, Long lectureId, String lectureTitle, 
                             Long memberId, String status, LocalDateTime enrolledAt) {
        this.enrollmentId = enrollmentId;
        this.lectureId = lectureId;
        this.lectureTitle = lectureTitle;
        this.memberId = memberId;
        this.status = status;
        this.enrolledAt = enrolledAt;
    }
}
