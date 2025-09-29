package com.company.wolbu.assignment.enrollment.controller;

import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.security.AuthenticatedUser;
import com.company.wolbu.assignment.auth.security.RequireRole;
import com.company.wolbu.assignment.common.dto.ApiResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentRequestDto;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentResponseDto;
import com.company.wolbu.assignment.enrollment.dto.EnrollmentResultDto;
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
@Tag(name = "수강 신청 API", description = "강의 수강 신청, 취소, 조회를 위한 API")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(
        summary = "강의 수강 신청",
        description = "여러 강의를 동시에 신청할 수 있습니다. 수강생 권한이 필요하며, 정원 초과 시 선착순으로 처리됩니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "신청할 강의 ID 목록",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EnrollmentRequestDto.class),
                examples = @ExampleObject(
                    name = "수강 신청 예시",
                    value = """
                        {
                            "lectureIds": [1, 2, 3]
                        }
                        """
                )
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신청 처리 완료 (부분 성공 포함)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강생 권한 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "정원 초과 또는 중복 신청")
    })
    @PostMapping
    @RequireRole(value = MemberRole.STUDENT, message = "수강 신청은 수강생만 할 수 있습니다.")
    public ResponseEntity<ApiResponseDto<EnrollmentResultDto>> enrollInLectures(
            AuthenticatedUser user,
            @Valid @RequestBody EnrollmentRequestDto request) {

        log.info("강의 신청 API 호출: memberId={}, lectureIds={}",
                user.getMemberId(), request.getLectureIds());

        EnrollmentResultDto result = enrollmentService.enrollInLectures(user.getMemberId(), request);

        // 모든 신청이 실패한 경우 적절한 HTTP 상태코드 반환
        if (result.getSuccessCount() == 0) {
            // 정원 초과가 주요 원인인 경우 409 CONFLICT 반환
            boolean hasCapacityError = result.getFailedEnrollments().stream()
                    .anyMatch(failure -> "COURSE_FULL".equals(failure.getErrorCode()));

            if (hasCapacityError) {
                return ResponseEntity.status(409).body(ApiResponseDto.error("COURSE_FULL", "정원이 초과되었습니다.", result));
            }
        }

        return ResponseEntity.ok(ApiResponseDto.success(result));
    }

    @Operation(
        summary = "내 수강 신청 목록 조회",
        description = "로그인한 사용자의 수강 신청 목록을 조회합니다. 수강생 권한이 필요합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강생 권한 필요")
    })
    @GetMapping("/my")
    @RequireRole(value = MemberRole.STUDENT, message = "수강 신청 목록은 수강생만 조회할 수 있습니다.")
    public ResponseEntity<ApiResponseDto<List<EnrollmentResponseDto>>> getMyEnrollments(
            AuthenticatedUser user) {

        log.info("내 수강 신청 목록 조회 API 호출: memberId={}", user.getMemberId());

        List<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByMember(user.getMemberId());
        return ResponseEntity.ok(ApiResponseDto.success(enrollments));
    }

    @Operation(
        summary = "수강 신청 취소",
        description = "본인의 수강 신청을 취소합니다. 소프트 삭제로 처리되어 재수강이 가능합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        parameters = @Parameter(name = "enrollmentId", description = "취소할 수강 신청 ID", example = "1", required = true)
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강생 권한 필요 또는 본인 신청이 아님"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "수강 신청을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 취소된 신청")
    })
    @DeleteMapping("/{enrollmentId}")
    @RequireRole(value = MemberRole.STUDENT, message = "수강 신청 취소는 수강생만 할 수 있습니다.")
    public ResponseEntity<ApiResponseDto<Void>> cancelEnrollment(
            AuthenticatedUser user,
            @PathVariable Long enrollmentId) {

        log.info("수강 신청 취소 API 호출: memberId={}, enrollmentId={}",
                user.getMemberId(), enrollmentId);

        enrollmentService.cancelEnrollment(user.getMemberId(), enrollmentId);
        return ResponseEntity.ok(ApiResponseDto.success(null));
    }
}
