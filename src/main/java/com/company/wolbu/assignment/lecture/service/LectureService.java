package com.company.wolbu.assignment.lecture.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.wolbu.assignment.auth.domain.Member;
import com.company.wolbu.assignment.auth.repository.MemberRepository;
import com.company.wolbu.assignment.common.exception.DomainException;
import com.company.wolbu.assignment.lecture.domain.Lecture;
import com.company.wolbu.assignment.lecture.dto.CreateLectureRequest;
import com.company.wolbu.assignment.lecture.dto.CreateLectureResponse;
import com.company.wolbu.assignment.lecture.dto.LectureListResponse;
import com.company.wolbu.assignment.lecture.dto.LectureSortType;
import com.company.wolbu.assignment.lecture.repository.LectureRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 강의 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final MemberRepository memberRepository;

    /**
     * 강의 개설
     * 강사만 강의를 개설할 수 있습니다.
     * 
     * @param memberId 요청한 회원 ID
     * @param request 강의 개설 요청 정보
     * @return 생성된 강의 정보
     * @throws DomainException 회원을 찾을 수 없거나 강사가 아닌 경우
     */
    @Transactional
    public CreateLectureResponse createLecture(Long memberId, CreateLectureRequest request) {
        log.info("강의 개설 요청: memberId={}, title={}", memberId, request.getTitle());
        
        // 1. 회원 존재 여부 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DomainException("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."));
        
        // 2. 강사 권한 확인
        if (!member.isInstructor()) {
            log.warn("강의 개설 권한 없음: memberId={}, role={}", memberId, member.getRole());
            throw new DomainException("INSTRUCTOR_ONLY", "강의는 강사만 개설할 수 있습니다.");
        }
        
        // 3. 강의 생성 및 저장
        try {
            Lecture lecture = Lecture.create(
                request.getTitle(),
                request.getMaxCapacity(),
                request.getPrice(),
                memberId
            );
            
            Lecture savedLecture = lectureRepository.save(lecture);
            
            log.info("강의 개설 완료: lectureId={}, instructorId={}", savedLecture.getId(), memberId);
            
            return new CreateLectureResponse(
                savedLecture.getId(),
                savedLecture.getTitle(),
                savedLecture.getMaxCapacity(),
                savedLecture.getPrice(),
                savedLecture.getInstructorId(),
                savedLecture.getCreatedAt()
            );
            
        } catch (IllegalArgumentException e) {
            log.error("강의 생성 실패 - 유효성 검증 오류: {}", e.getMessage());
            throw new DomainException("INVALID_LECTURE_DATA", e.getMessage());
        }
    }

    /**
     * 강의 정보 조회
     * 
     * @param lectureId 강의 ID
     * @return 강의 정보
     * @throws DomainException 강의를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public CreateLectureResponse getLecture(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new DomainException("LECTURE_NOT_FOUND", "강의를 찾을 수 없습니다."));
        
        return new CreateLectureResponse(
            lecture.getId(),
            lecture.getTitle(),
            lecture.getMaxCapacity(),
            lecture.getPrice(),
            lecture.getInstructorId(),
            lecture.getCreatedAt()
        );
    }

    /**
     * 강사의 강의 권한 확인
     * 
     * @param lectureId 강의 ID
     * @param instructorId 강사 ID
     * @return 권한이 있으면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    public boolean hasInstructorPermission(Long lectureId, Long instructorId) {
        return lectureRepository.findByIdAndInstructorId(lectureId, instructorId).isPresent();
    }

    /**
     * 강의 목록 조회 (페이징 및 정렬)
     * 
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기 (기본값 20, 최대 100)
     * @param sortType 정렬 타입
     * @return 강의 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<LectureListResponse> getLectureList(Integer page, Integer size, LectureSortType sortType) {
        // 페이지 번호 검증 (1부터 시작, 0으로 변환)
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        
        // 페이지 크기 검증 (기본값 20, 최대 100)
        int pageSize = (size != null && size > 0) ? Math.min(size, 100) : 20;
        
        // 정렬 타입 기본값 설정
        LectureSortType sort = (sortType != null) ? sortType : LectureSortType.RECENT;
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        
        log.info("강의 목록 조회: page={}, size={}, sort={}", pageNumber + 1, pageSize, sort);
        
        // 정렬 타입에 따른 조회
        switch (sort) {
            case POPULAR_COUNT:
                return lectureRepository.findAllWithEnrollmentCountOrderByEnrollmentCount(pageable);
            case POPULAR_RATE:
                return lectureRepository.findAllWithEnrollmentCountOrderByEnrollmentRate(pageable);
            case RECENT:
            default:
                return lectureRepository.findAllWithEnrollmentCountOrderByCreatedAt(pageable);
        }
    }
}
