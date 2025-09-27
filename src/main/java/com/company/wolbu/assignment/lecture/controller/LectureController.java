package com.company.wolbu.assignment.lecture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.security.AuthenticatedUser;
import com.company.wolbu.assignment.auth.security.RequireRole;
import com.company.wolbu.assignment.dto.ApiResponse;
import com.company.wolbu.assignment.lecture.dto.CreateLectureRequest;
import com.company.wolbu.assignment.lecture.dto.CreateLectureResponse;
import com.company.wolbu.assignment.lecture.service.LectureService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 강의 관련 API를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/lectures")
@Validated
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    /**
     * 강의 개설
     * @RequireRole 어노테이션을 통해 강사만 접근 가능하도록 제한합니다.
     * 
     * @param user 인증된 사용자 정보 (JWT 토큰에서 자동 추출)
     * @param request 강의 개설 요청 정보
     * @return 생성된 강의 정보
     */
    @PostMapping
    @RequireRole(value = MemberRole.INSTRUCTOR, message = "강의는 강사만 개설할 수 있습니다.")
    public ResponseEntity<ApiResponse<CreateLectureResponse>> createLecture(
            AuthenticatedUser user,
            @Valid @RequestBody CreateLectureRequest request) {
        
        log.info("강의 개설 API 호출: title={}, instructorId={}", request.getTitle(), user.getMemberId());
        
        // 강의 개설
        CreateLectureResponse response = lectureService.createLecture(user.getMemberId(), request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 강의 정보 조회
     * 
     * @param lectureId 조회할 강의 ID
     * @return 강의 정보
     */
    @GetMapping("/{lectureId}")
    public ResponseEntity<ApiResponse<CreateLectureResponse>> getLecture(@PathVariable Long lectureId) {
        log.info("강의 조회 API 호출: lectureId={}", lectureId);
        
        CreateLectureResponse response = lectureService.getLecture(lectureId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
