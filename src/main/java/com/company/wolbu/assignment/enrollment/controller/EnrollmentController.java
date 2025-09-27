package com.company.wolbu.assignment.enrollment.controller;

import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.security.AuthenticatedUser;
import com.company.wolbu.assignment.auth.security.RequireRole;
import com.company.wolbu.assignment.common.dto.ApiResponse;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentRequest;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentResponse;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentResult;
import com.company.wolbu.assignment.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 수강 신청 관련 API를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/enrollments")
@Validated
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * 강의 신청 여러 강의를 동시에 신청할 수 있습니다.
     *
     * @param user    인증된 사용자 정보
     * @param request 신청할 강의 목록
     * @return 신청 결과 (성공/실패 목록)
     */
    @PostMapping
    @RequireRole(value = MemberRole.STUDENT, message = "수강 신청은 수강생만 할 수 있습니다.")
    public ResponseEntity<ApiResponse<EnrollmentResult>> enrollInLectures(
            AuthenticatedUser user,
            @Valid @RequestBody EnrollmentRequest request) {

        log.info("강의 신청 API 호출: memberId={}, lectureIds={}",
                user.getMemberId(), request.getLectureIds());

        EnrollmentResult result = enrollmentService.enrollInLectures(user.getMemberId(), request);

        // 모든 신청이 실패한 경우 적절한 HTTP 상태코드 반환
        if (result.getSuccessCount() == 0) {
            // 정원 초과가 주요 원인인 경우 409 CONFLICT 반환
            boolean hasCapacityError = result.getFailedEnrollments().stream()
                    .anyMatch(failure -> "COURSE_FULL".equals(failure.getErrorCode()));

            if (hasCapacityError) {
                return ResponseEntity.status(409).body(ApiResponse.error("COURSE_FULL", "정원이 초과되었습니다.", result));
            }
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 내 수강 신청 목록 조회
     *
     * @param user 인증된 사용자 정보
     * @return 수강 신청 목록
     */
    @GetMapping("/my")
    @RequireRole(value = MemberRole.STUDENT, message = "수강 신청 목록은 수강생만 조회할 수 있습니다.")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments(
            AuthenticatedUser user) {

        log.info("내 수강 신청 목록 조회 API 호출: memberId={}", user.getMemberId());

        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByMember(user.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    /**
     * 수강 신청 취소
     *
     * @param user         인증된 사용자 정보
     * @param enrollmentId 취소할 수강 신청 ID
     * @return 취소 결과
     */
    @DeleteMapping("/{enrollmentId}")
    @RequireRole(value = MemberRole.STUDENT, message = "수강 신청 취소는 수강생만 할 수 있습니다.")
    public ResponseEntity<ApiResponse<Void>> cancelEnrollment(
            AuthenticatedUser user,
            @PathVariable Long enrollmentId) {

        log.info("수강 신청 취소 API 호출: memberId={}, enrollmentId={}",
                user.getMemberId(), enrollmentId);

        enrollmentService.cancelEnrollment(user.getMemberId(), enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
