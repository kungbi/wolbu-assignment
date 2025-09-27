package com.company.wolbu.assignment.lecture.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 강의 개설 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateLectureRequest {

    @NotBlank(message = "강의명은 필수입니다.")
    @Size(max = 200, message = "강의명은 200자를 초과할 수 없습니다.")
    private String title;

    @NotNull(message = "최대 수강 인원은 필수입니다.")
    @Min(value = 1, message = "최대 수강 인원은 1명 이상이어야 합니다.")
    private Integer maxCapacity;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    public CreateLectureRequest(String title, Integer maxCapacity, Integer price) {
        this.title = title;
        this.maxCapacity = maxCapacity;
        this.price = price;
    }
}
