package com.company.wolbu.assignment.enrollment.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 신청 요청 DTO
 */
@Getter
@NoArgsConstructor
public class EnrollmentRequestDto {
    
    /**
     * 신청할 강의 ID 목록
     */
    @NotEmpty(message = "신청할 강의를 선택해주세요.")
    private List<@NotNull(message = "강의 ID는 필수입니다.") Long> lectureIds;

    public EnrollmentRequestDto(List<Long> lectureIds) {
        this.lectureIds = lectureIds;
    }
}
