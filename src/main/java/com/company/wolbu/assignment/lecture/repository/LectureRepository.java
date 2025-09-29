package com.company.wolbu.assignment.lecture.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import com.company.wolbu.assignment.lecture.domain.Lecture;
import com.company.wolbu.assignment.lecture.dto.LectureListResponseDto;

/**
 * 강의 Repository 인터페이스
 */
@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

    /**
     * 강사 ID로 강의 목록 조회
     * @param instructorId 강사 ID
     * @return 해당 강사의 강의 목록
     */
    List<Lecture> findByInstructorId(Long instructorId);

    /**
     * 강의 ID와 강사 ID로 강의 조회 (권한 확인용)
     * @param id 강의 ID
     * @param instructorId 강사 ID
     * @return 해당 강사의 강의 (Optional)
     */
    @Query("SELECT l FROM Lecture l WHERE l.id = :id AND l.instructorId = :instructorId")
    Optional<Lecture> findByIdAndInstructorId(@Param("id") Long id, @Param("instructorId") Long instructorId);

    /**
     * 강의 제목으로 검색 (부분 일치)
     * @param title 검색할 강의 제목
     * @return 제목에 해당 문자열이 포함된 강의 목록
     */
    @Query("SELECT l FROM Lecture l WHERE l.title LIKE %:title%")
    List<Lecture> findByTitleContaining(@Param("title") String title);

    /**
     * 강의 목록 조회 (최근 등록순)
     * 
     * @param pageable 페이징 정보
     * @return 강의 목록과 신청자 수 정보
     */
    @Query("SELECT new com.company.wolbu.assignment.lecture.dto.LectureListResponseDto(" +
           "l.id, l.title, l.price, m.name, " +
           "COALESCE(COUNT(e), 0), l.maxCapacity, l.createdAt) " +
           "FROM Lecture l " +
           "LEFT JOIN Member m ON l.instructorId = m.id " +
           "LEFT JOIN Enrollment e ON l.id = e.lectureId AND e.status = 'CONFIRMED' " +
           "GROUP BY l.id, l.title, l.price, m.name, l.maxCapacity, l.createdAt " +
           "ORDER BY l.createdAt DESC")
    Page<LectureListResponseDto> findAllWithEnrollmentCountOrderByCreatedAt(Pageable pageable);

    /**
     * 강의 목록 조회 (신청자 많은 순)
     * 
     * @param pageable 페이징 정보
     * @return 강의 목록과 신청자 수 정보
     */
    @Query("SELECT new com.company.wolbu.assignment.lecture.dto.LectureListResponseDto(" +
           "l.id, l.title, l.price, m.name, " +
           "COALESCE(COUNT(e), 0), l.maxCapacity, l.createdAt) " +
           "FROM Lecture l " +
           "LEFT JOIN Member m ON l.instructorId = m.id " +
           "LEFT JOIN Enrollment e ON l.id = e.lectureId AND e.status = 'CONFIRMED' " +
           "GROUP BY l.id, l.title, l.price, m.name, l.maxCapacity, l.createdAt " +
           "ORDER BY COUNT(e) DESC, l.createdAt DESC")
    Page<LectureListResponseDto> findAllWithEnrollmentCountOrderByEnrollmentCount(Pageable pageable);

    /**
     * 강의 목록 조회 (신청률 높은 순)
     * 
     * @param pageable 페이징 정보
     * @return 강의 목록과 신청자 수 정보
     */
    @Query("SELECT new com.company.wolbu.assignment.lecture.dto.LectureListResponseDto(" +
           "l.id, l.title, l.price, m.name, " +
           "COALESCE(COUNT(e), 0), l.maxCapacity, l.createdAt) " +
           "FROM Lecture l " +
           "LEFT JOIN Member m ON l.instructorId = m.id " +
           "LEFT JOIN Enrollment e ON l.id = e.lectureId AND e.status = 'CONFIRMED' " +
           "GROUP BY l.id, l.title, l.price, m.name, l.maxCapacity, l.createdAt " +
           "ORDER BY (CAST(COUNT(e) AS double) / l.maxCapacity) DESC, l.createdAt DESC")
    Page<LectureListResponseDto> findAllWithEnrollmentCountOrderByEnrollmentRate(Pageable pageable);

    /**
     * 동시성 제어를 위한 강의 조회 (비관적 락)
     * 수강 신청/취소 시 정원 체크를 위해 사용합니다.
     * 
     * @param id 강의 ID
     * @return 강의 정보 (비관적 락 적용)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lecture l WHERE l.id = :id")
    Optional<Lecture> findByIdWithLock(@Param("id") Long id);
}
