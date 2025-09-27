package com.company.wolbu.assignment.enrollment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수강 신청 도메인 엔티티
 * 소프트 삭제 정책을 적용하여 재수강을 허용합니다.
 * 활성 등록(CONFIRMED)만 중복 제약과 정원 계산에 포함됩니다.
 */
@Entity
@Table(
    name = "enrollments",
    indexes = {
        @Index(name = "idx_lecture_member_status", columnList = "lecture_id, member_id, status"),
        @Index(name = "idx_lecture_status", columnList = "lecture_id, status"),
        @Index(name = "idx_member_status", columnList = "member_id, status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime canceledAt;

    /**
     * 수강 신청 생성 팩토리 메서드
     * 
     * @param lectureId 강의 ID
     * @param memberId 회원 ID
     * @return 생성된 수강 신청 엔티티
     */
    public static Enrollment create(Long lectureId, Long memberId) {
        validateLectureId(lectureId);
        validateMemberId(memberId);
        
        Enrollment enrollment = new Enrollment();
        enrollment.lectureId = lectureId;
        enrollment.memberId = memberId;
        enrollment.status = EnrollmentStatus.CONFIRMED;
        enrollment.createdAt = LocalDateTime.now();
        enrollment.updatedAt = LocalDateTime.now();
        return enrollment;
    }

    /**
     * 수강 신청 취소 (소프트 삭제)
     * 재수강을 위해 물리적 삭제 대신 상태만 변경합니다.
     */
    public void cancel() {
        if (this.status == EnrollmentStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 수강 신청입니다.");
        }
        this.status = EnrollmentStatus.CANCELED;
        this.updatedAt = LocalDateTime.now();
        this.canceledAt = LocalDateTime.now();
    }

    /**
     * 활성 상태인지 확인 (정원 계산 대상)
     */
    public boolean isActive() {
        return this.status == EnrollmentStatus.CONFIRMED;
    }

    /**
     * 취소 상태인지 확인
     */
    public boolean isCanceled() {
        return this.status == EnrollmentStatus.CANCELED;
    }

    /**
     * 재활성화 (재수강)
     * 취소된 신청을 다시 활성화합니다.
     */
    public void reactivate() {
        if (this.status == EnrollmentStatus.CONFIRMED) {
            throw new IllegalStateException("이미 활성 상태인 수강 신청입니다.");
        }
        this.status = EnrollmentStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
        this.canceledAt = null;
    }

    /**
     * 강의 ID 유효성 검증
     */
    private static void validateLectureId(Long lectureId) {
        if (lectureId == null || lectureId <= 0) {
            throw new IllegalArgumentException("강의 ID는 필수이며 양수여야 합니다.");
        }
    }

    /**
     * 회원 ID 유효성 검증
     */
    private static void validateMemberId(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("회원 ID는 필수이며 양수여야 합니다.");
        }
    }
}
