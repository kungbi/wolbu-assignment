package com.company.wolbu.assignment.lecture.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import com.company.wolbu.assignment.lecture.dto.CreateLectureRequestDto;
import com.company.wolbu.assignment.lecture.dto.CreateLectureResponseDto;
import com.company.wolbu.assignment.lecture.dto.LectureListResponseDto;
import com.company.wolbu.assignment.lecture.dto.LectureSortTypeDto;
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
@Tag(name = "강의 API", description = "강의 개설, 조회, 목록 조회를 위한 API")
public class LectureController {

    private final LectureService lectureService;

    @Operation(
        summary = "강의 개설",
        description = "새로운 강의를 개설합니다. 강사 권한이 필요합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "강의 개설 정보",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateLectureRequestDto.class),
                examples = @ExampleObject(
                    name = "강의 개설 예시",
                    value = """
                        {
                            "title": "Spring Boot 마스터 클래스",
                            "maxCapacity": 30,
                            "price": 150000
                        }
                        """
                )
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강의 개설 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "강사 권한 필요")
    })
    @PostMapping
    @RequireRole(value = MemberRole.INSTRUCTOR, message = "강의는 강사만 개설할 수 있습니다.")
    public ResponseEntity<ApiResponseDto<CreateLectureResponseDto>> createLecture(
            AuthenticatedUser user,
            @Valid @RequestBody CreateLectureRequestDto request) {
        
        log.info("강의 개설 API 호출: title={}, instructorId={}", request.getTitle(), user.getMemberId());
        
        // 강의 개설
        CreateLectureResponseDto response = lectureService.createLecture(user.getMemberId(), request);
        
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @Operation(
        summary = "강의 목록 조회",
        description = "페이징과 정렬 옵션을 지원하는 강의 목록을 조회합니다. 인증 없이 접근 가능합니다.",
        parameters = {
            @Parameter(name = "page", description = "페이지 번호 (1부터 시작)", example = "1"),
            @Parameter(name = "size", description = "페이지 크기 (최대 100)", example = "20"),
            @Parameter(name = "sort", description = "정렬 방식", example = "RECENT",
                schema = @Schema(implementation = LectureSortTypeDto.class))
        }
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강의 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터")
    })
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<LectureListResponseDto>>> getLectureList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "sort", defaultValue = "RECENT") LectureSortTypeDto sort) {
        
        log.info("강의 목록 조회 API 호출: page={}, size={}, sort={}", page, size, sort);
        
        Page<LectureListResponseDto> lectureList = lectureService.getLectureList(page, size, sort);
        return ResponseEntity.ok(ApiResponseDto.success(lectureList));
    }

    @Operation(
        summary = "강의 상세 조회",
        description = "강의 ID로 특정 강의의 상세 정보를 조회합니다. 인증 없이 접근 가능합니다.",
        parameters = @Parameter(name = "lectureId", description = "강의 ID", example = "1", required = true)
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강의 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음")
    })
    @GetMapping("/{lectureId}")
    public ResponseEntity<ApiResponseDto<CreateLectureResponseDto>> getLecture(@PathVariable Long lectureId) {
        log.info("강의 조회 API 호출: lectureId={}", lectureId);
        
        CreateLectureResponseDto response = lectureService.getLecture(lectureId);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }
}
