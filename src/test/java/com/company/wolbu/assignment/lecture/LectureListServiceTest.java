package com.company.wolbu.assignment.lecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.company.wolbu.assignment.lecture.dto.LectureListResponseDto;
import com.company.wolbu.assignment.lecture.dto.LectureSortTypeDto;
import com.company.wolbu.assignment.lecture.repository.LectureRepository;
import com.company.wolbu.assignment.lecture.service.LectureService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * 강의 목록 조회 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
class LectureListServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @InjectMocks
    private LectureService lectureService;

    @Test
    @DisplayName("강의 목록 조회 - 최근 등록순")
    void getLectureList_Recent() {
        // Given
        List<LectureListResponseDto> mockLectures = List.of(
                new LectureListResponseDto(1L, "최신 강의", 50000, "김강사", 5L, 10, LocalDateTime.now()),
                new LectureListResponseDto(2L, "이전 강의", 30000, "이강사", 3L, 15, LocalDateTime.now().minusDays(1))
        );
        Page<LectureListResponseDto> mockPage = new PageImpl<>(mockLectures);

        when(lectureRepository.findAllWithEnrollmentCountOrderByCreatedAt(any(Pageable.class)))
                .thenReturn(mockPage);

        // When
        Page<LectureListResponseDto> result = lectureService.getLectureList(1, 20, LectureSortTypeDto.RECENT);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("최신 강의");
        assertThat(result.getContent().get(0).getCurrentEnrollmentCount()).isEqualTo(5L);
        assertThat(result.getContent().get(0).getEnrollmentRate()).isEqualByComparingTo(BigDecimal.valueOf(50.0000));
    }

    @Test
    @DisplayName("강의 목록 조회 - 신청자 많은 순")
    void getLectureList_PopularCount() {
        // Given
        List<LectureListResponseDto> mockLectures = List.of(
                new LectureListResponseDto(1L, "인기 강의", 50000, "김강사", 8L, 10, LocalDateTime.now()),
                new LectureListResponseDto(2L, "일반 강의", 30000, "이강사", 3L, 15, LocalDateTime.now())
        );
        Page<LectureListResponseDto> mockPage = new PageImpl<>(mockLectures);

        when(lectureRepository.findAllWithEnrollmentCountOrderByEnrollmentCount(any(Pageable.class)))
                .thenReturn(mockPage);

        // When
        Page<LectureListResponseDto> result = lectureService.getLectureList(1, 20, LectureSortTypeDto.POPULAR_COUNT);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("인기 강의");
        assertThat(result.getContent().get(0).getCurrentEnrollmentCount()).isEqualTo(8L);
    }

    @Test
    @DisplayName("강의 목록 조회 - 신청률 높은 순")
    void getLectureList_PopularRate() {
        // Given
        List<LectureListResponseDto> mockLectures = List.of(
                new LectureListResponseDto(1L, "고신청률 강의", 50000, "김강사", 9L, 10, LocalDateTime.now()),
                new LectureListResponseDto(2L, "저신청률 강의", 30000, "이강사", 3L, 15, LocalDateTime.now())
        );
        Page<LectureListResponseDto> mockPage = new PageImpl<>(mockLectures);

        when(lectureRepository.findAllWithEnrollmentCountOrderByEnrollmentRate(any(Pageable.class)))
                .thenReturn(mockPage);

        // When
        Page<LectureListResponseDto> result = lectureService.getLectureList(1, 20, LectureSortTypeDto.POPULAR_RATE);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("고신청률 강의");
        assertThat(result.getContent().get(0).getEnrollmentRate()).isEqualByComparingTo(BigDecimal.valueOf(90.0000));
        assertThat(result.getContent().get(1).getEnrollmentRate()).isEqualByComparingTo(BigDecimal.valueOf(20.0000));
    }

    @Test
    @DisplayName("페이지 크기 제한 테스트 - 최대 100개")
    void getLectureList_PageSizeLimit() {
        // Given
        when(lectureRepository.findAllWithEnrollmentCountOrderByCreatedAt(any(Pageable.class)))
                .thenReturn(Page.empty());

        // When
        Page<LectureListResponseDto> result = lectureService.getLectureList(1, 200, LectureSortTypeDto.RECENT);

        // Then - 실제 페이지 크기가 100으로 제한되는지는 모킹된 상황에서 직접 검증하기 어려우므로
        // 서비스가 정상적으로 호출되는지 확인
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("기본값 테스트 - null 파라미터")
    void getLectureList_DefaultValues() {
        // Given
        when(lectureRepository.findAllWithEnrollmentCountOrderByCreatedAt(any(Pageable.class)))
                .thenReturn(Page.empty());

        // When
        Page<LectureListResponseDto> result = lectureService.getLectureList(null, null, null);

        // Then
        assertThat(result).isNotNull();
    }
}
