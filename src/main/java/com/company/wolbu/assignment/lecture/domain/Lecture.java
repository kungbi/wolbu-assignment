package com.company.wolbu.assignment.lecture.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 도메인 엔티티
 * 강사가 개설하는 강의를 나타냅니다.
 */
@Entity
@Table(name = "lectures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Long instructorId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 강의 생성 팩토리 메서드
     * @param title 강의명
     * @param maxCapacity 최대 수강 인원
     * @param price 가격
     * @param instructorId 강사 ID
     * @return 생성된 강의 엔티티
     */
    public static Lecture create(String title, Integer maxCapacity, Integer price, Long instructorId) {
        validateTitle(title);
        validateMaxCapacity(maxCapacity);
        validatePrice(price);
        
        Lecture lecture = new Lecture();
        lecture.title = title;
        lecture.maxCapacity = maxCapacity;
        lecture.price = price;
        lecture.instructorId = instructorId;
        lecture.createdAt = LocalDateTime.now();
        return lecture;
    }

    /**
     * 강의명 유효성 검증
     */
    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("강의명은 필수입니다.");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("강의명은 200자를 초과할 수 없습니다.");
        }
    }

    /**
     * 최대 수강 인원 유효성 검증
     */
    private static void validateMaxCapacity(Integer maxCapacity) {
        if (maxCapacity == null || maxCapacity < 1) {
            throw new IllegalArgumentException("최대 수강 인원은 1명 이상이어야 합니다.");
        }
        if (maxCapacity > 1000) {
            throw new IllegalArgumentException("최대 수강 인원은 1000명을 초과할 수 없습니다.");
        }
    }

    /**
     * 가격 유효성 검증
     */
    private static void validatePrice(Integer price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }
        if (price > 10000000) {
            throw new IllegalArgumentException("가격은 1천만원을 초과할 수 없습니다.");
        }
    }

    /**
     * 강사 ID 확인
     */
    public boolean isInstructorOf(Long instructorId) {
        return this.instructorId.equals(instructorId);
    }
}
