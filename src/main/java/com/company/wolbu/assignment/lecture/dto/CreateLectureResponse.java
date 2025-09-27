package com.company.wolbu.assignment.lecture.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 개설 응답 DTO
 */
@Getter
@NoArgsConstructor
public class CreateLectureResponse {

    private Long id;
    private String title;
    private Integer maxCapacity;
    private Integer price;
    private Long instructorId;
    private LocalDateTime createdAt;

    public CreateLectureResponse(Long id, String title, Integer maxCapacity, Integer price, Long instructorId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.maxCapacity = maxCapacity;
        this.price = price;
        this.instructorId = instructorId;
        this.createdAt = createdAt;
    }
}
