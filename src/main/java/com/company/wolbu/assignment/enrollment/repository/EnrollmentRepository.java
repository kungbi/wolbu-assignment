package com.company.wolbu.assignment.enrollment.repository;

import com.company.wolbu.assignment.enrollment.domain.Enrollment;
import com.company.wolbu.assignment.enrollment.domain.EnrollmentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 수강 신청 Repository 인터페이스
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * 특정 강의의 활성 수강 신청 수 조회 (정원 계산용)
     *
     * @param lectureId 강의 ID
     * @return 활성 상태 수강 신청 수
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.lectureId = :lectureId AND e.status = 'CONFIRMED'")
    long countActiveByLectureId(@Param("lectureId") Long lectureId);

    /**
     * 특정 강의의 상태별 수강 신청 수 조회
     *
     * @param lectureId 강의 ID
     * @param status    수강 신청 상태
     * @return 해당 상태의 수강 신청 수
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.lectureId = :lectureId AND e.status = :status")
    long countByLectureIdAndStatus(@Param("lectureId") Long lectureId, @Param("status") EnrollmentStatus status);

    /**
     * 회원의 특정 강의 수강 신청 조회 (UNIQUE 제약조건으로 하나만 존재)
     *
     * @param lectureId 강의 ID
     * @param memberId  회원 ID
     * @return 수강 신청 정보 (Optional) - 활성/취소 상태 모두 포함
     */
    Optional<Enrollment> findByLectureIdAndMemberId(Long lectureId, Long memberId);

    /**
     * 회원의 수강 신청 목록 조회
     *
     * @param memberId 회원 ID
     * @return 해당 회원의 수강 신청 목록
     */
    List<Enrollment> findByMemberId(Long memberId);

    /**
     * 특정 강의의 수강 신청 목록 조회
     *
     * @param lectureId 강의 ID
     * @return 해당 강의의 수강 신청 목록
     */
    List<Enrollment> findByLectureId(Long lectureId);

    /**
     * 회원의 특정 상태 수강 신청 목록 조회
     *
     * @param memberId 회원 ID
     * @param status   수강 신청 상태
     * @return 해당 회원의 특정 상태 수강 신청 목록
     */
    @EntityGraph(attributePaths = {"lecture"})
    List<Enrollment> findByMemberIdAndStatus(Long memberId, EnrollmentStatus status);

    /**
     * 회원의 특정 강의 활성 수강 신청 존재 여부 확인 (중복 체크용)
     *
     * @param lectureId 강의 ID
     * @param memberId  회원 ID
     * @return 활성 상태 수강 신청이 존재하면 true
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
           "FROM Enrollment e WHERE e.lectureId = :lectureId AND e.memberId = :memberId AND e.status = 'CONFIRMED'")
    boolean existsActiveByLectureIdAndMemberId(@Param("lectureId") Long lectureId, @Param("memberId") Long memberId);


}
