package com.company.wolbu.assignment.lecture.dto;

/**
 * 강의 목록 정렬 타입
 */
public enum LectureSortTypeDto {
    /**
     * 최근 등록순 (등록일 DESC)
     */
    RECENT,
    
    /**
     * 신청자 많은 순 (신청자 수 DESC)
     */
    POPULAR_COUNT,
    
    /**
     * 신청률 높은 순 (신청자 수 ÷ 최대 정원 DESC)
     */
    POPULAR_RATE
}
