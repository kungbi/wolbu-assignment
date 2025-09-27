package com.company.wolbu.assignment.enrollment.domain;

/**
 * 수강 신청 상태를 나타내는 열거형
 * 소프트 삭제 정책을 지원합니다.
 */
public enum EnrollmentStatus {
    /**
     * 활성 상태 (신청 확정)
     * 정원 계산 및 중복 체크에 포함됩니다.
     */
    CONFIRMED,
    
    /**
     * 취소 상태 (소프트 삭제)
     * 정원 계산에서 제외되며, 재수강이 가능합니다.
     */
    CANCELED
}
