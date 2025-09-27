package com.company.wolbu.assignment.enrollment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.wolbu.assignment.auth.domain.Member;
import com.company.wolbu.assignment.auth.repository.MemberRepository;
import com.company.wolbu.assignment.enrollment.domain.Enrollment;
import com.company.wolbu.assignment.enrollment.domain.EnrollmentStatus;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentRequest;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentResponse;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentResult;
import com.company.wolbu.assignment.enrollment.repository.EnrollmentRepository;
import com.company.wolbu.assignment.enrollment.exception.AlreadyEnrolledException;
import com.company.wolbu.assignment.enrollment.exception.AlreadyCanceledException;
import com.company.wolbu.assignment.enrollment.exception.CourseFullException;
import com.company.wolbu.assignment.enrollment.exception.EnrollmentNotFoundException;
import com.company.wolbu.assignment.enrollment.exception.MemberNotFoundException;
import com.company.wolbu.assignment.enrollment.exception.UnauthorizedEnrollmentException;
import com.company.wolbu.assignment.lecture.domain.Lecture;
import com.company.wolbu.assignment.lecture.exception.LectureNotFoundException;
import com.company.wolbu.assignment.lecture.repository.LectureRepository;
import com.company.wolbu.assignment.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 수강 신청 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LectureRepository lectureRepository;
    private final MemberRepository memberRepository;

    /**
     * 여러 강의에 동시 신청
     * 
     * @param memberId 회원 ID
     * @param request 수강 신청 요청
     * @return 신청 결과
     */
    @Transactional
    public EnrollmentResult enrollInLectures(Long memberId, EnrollmentRequest request) {
        log.info("강의 신청 요청: memberId={}, lectureIds={}", memberId, request.getLectureIds());
        
        // 1. 회원 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        
        List<EnrollmentResponse> successfulEnrollments = new ArrayList<>();
        List<EnrollmentResult.EnrollmentFailure> failedEnrollments = new ArrayList<>();
        
        // 2. 데드락 방지를 위해 강의 ID 정렬
        List<Long> sortedLectureIds = new ArrayList<>(request.getLectureIds());
        sortedLectureIds.sort(Long::compareTo);
        
        // 3. 각 강의에 대해 순차적으로 신청 처리
        for (Long lectureId : sortedLectureIds) {
            try {
                EnrollmentResponse response = enrollInSingleLecture(memberId, lectureId);
                successfulEnrollments.add(response);
                log.info("강의 신청 성공: memberId={}, lectureId={}", memberId, lectureId);
                
            } catch (BusinessException e) {
                // 비즈니스 예외인 경우 실패 목록에 추가
                Lecture lecture = lectureRepository.findById(lectureId)
                        .orElseThrow(() -> new LectureNotFoundException(lectureId));
                String lectureTitle = lecture.getTitle();
                
                failedEnrollments.add(new EnrollmentResult.EnrollmentFailure(
                    lectureId, lectureTitle, e.errorCode(), e.getMessage()));
                
                log.warn("강의 신청 실패: memberId={}, lectureId={}, error={}", 
                         memberId, lectureId, e.getMessage());
            }
        }
        
        return new EnrollmentResult(successfulEnrollments, failedEnrollments);
    }

    /**
     * 단일 강의 신청 처리
     * 
     * @param memberId 회원 ID
     * @param lectureId 강의 ID
     * @return 신청 응답
     */
    private EnrollmentResponse enrollInSingleLecture(Long memberId, Long lectureId) {
        // 1. 강의 존재 확인 및 비관적 락 획득
        Lecture lecture = lectureRepository.findByIdWithLock(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));
        
        // 2. 활성 중복 신청 확인 (CONFIRMED 상태만)
        if (enrollmentRepository.existsActiveByLectureIdAndMemberId(lectureId, memberId)) {
            throw new AlreadyEnrolledException(lectureId);
        }
        
        // 3. 현재 활성 신청자 수 확인 (CONFIRMED 상태만 계산)
        long currentActiveCount = enrollmentRepository.countActiveByLectureId(lectureId);
        
        if (currentActiveCount >= lecture.getMaxCapacity()) {
            throw new CourseFullException(lectureId, lecture.getMaxCapacity());
        }
        
        // 4. 기존 취소된 신청 이력 확인 (재수강 시 재활성화 가능)
        List<Enrollment> existingEnrollments = enrollmentRepository.findAllByLectureIdAndMemberId(lectureId, memberId);
        
        Enrollment enrollment;
        if (!existingEnrollments.isEmpty()) {
            // 기존 취소된 신청이 있으면 재활성화
            Enrollment canceledEnrollment = existingEnrollments.stream()
                    .filter(e -> e.isCanceled())
                    .findFirst()
                    .orElse(null);
            
            if (canceledEnrollment != null) {
                canceledEnrollment.reactivate();
                enrollment = enrollmentRepository.save(canceledEnrollment);
                log.info("기존 취소 신청 재활성화: enrollmentId={}, memberId={}, lectureId={}", 
                         enrollment.getId(), memberId, lectureId);
            } else {
                // 새로운 신청 생성
                enrollment = Enrollment.create(lectureId, memberId);
                enrollment = enrollmentRepository.save(enrollment);
            }
        } else {
            // 새로운 신청 생성
            enrollment = Enrollment.create(lectureId, memberId);
            enrollment = enrollmentRepository.save(enrollment);
        }
        
        return new EnrollmentResponse(
            enrollment.getId(),
            enrollment.getLectureId(),
            lecture.getTitle(),
            enrollment.getMemberId(),
            enrollment.getStatus().name(),
            enrollment.getCreatedAt()
        );
    }

    /**
     * 회원의 수강 신청 목록 조회
     * 
     * @param memberId 회원 ID
     * @return 수강 신청 목록
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByMember(Long memberId) {
        log.info("회원 수강 신청 목록 조회: memberId={}", memberId);
        
        // 회원 존재 확인
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException(memberId);
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByMemberIdAndStatus(
                memberId, EnrollmentStatus.CONFIRMED);
        
        return enrollments.stream()
                .map(enrollment -> {
                    Lecture lecture = lectureRepository.findById(enrollment.getLectureId())
                            .orElseThrow(() -> new LectureNotFoundException());
                    
                    return new EnrollmentResponse(
                        enrollment.getId(),
                        enrollment.getLectureId(),
                        lecture.getTitle(),
                        enrollment.getMemberId(),
                        enrollment.getStatus().name(),
                        enrollment.getCreatedAt()
                    );
                })
                .toList();
    }

    /**
     * 수강 신청 취소
     * 
     * @param memberId 회원 ID
     * @param enrollmentId 수강 신청 ID
     */
    @Transactional
    public void cancelEnrollment(Long memberId, Long enrollmentId) {
        log.info("수강 신청 취소 요청: memberId={}, enrollmentId={}", memberId, enrollmentId);
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));
        
        // 본인의 신청인지 확인
        if (!enrollment.getMemberId().equals(memberId)) {
            throw new UnauthorizedEnrollmentException();
        }
        
        // 이미 취소된 신청인지 확인
        if (enrollment.isCanceled()) {
            throw new AlreadyCanceledException(enrollmentId);
        }
        
        // 동시성 제어를 위해 강의에 락 획득
        Lecture lecture = lectureRepository.findByIdWithLock(enrollment.getLectureId())
                .orElseThrow(() -> new LectureNotFoundException(enrollment.getLectureId()));
        
        // 소프트 삭제 (상태 변경)
        enrollment.cancel();
        enrollmentRepository.save(enrollment);
        
        log.info("수강 신청 취소 완료: enrollmentId={}, memberId={}, lectureId={}", 
                 enrollmentId, memberId, enrollment.getLectureId());
    }
}