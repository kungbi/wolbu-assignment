package com.company.wolbu.assignment.lecture.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.company.wolbu.assignment.lecture.domain.Lecture;

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
}
