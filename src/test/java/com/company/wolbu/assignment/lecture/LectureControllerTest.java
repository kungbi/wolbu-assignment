package com.company.wolbu.assignment.lecture;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.company.wolbu.assignment.auth.domain.Member;
import com.company.wolbu.assignment.auth.domain.MemberRole;
import com.company.wolbu.assignment.auth.repository.MemberRepository;
import com.company.wolbu.assignment.auth.security.JwtProvider;
import com.company.wolbu.assignment.lecture.dto.CreateLectureRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 강의 컨트롤러 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LectureControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private JwtProvider jwtProvider;
    
    private String instructorToken;
    private String studentToken;
    private Long instructorId;
    private Long studentId;

    @BeforeEach
    void setUp() {
        // 테스트용 강사 생성
        Member instructor = Member.create("강사", "instructor@example.com", "010-1111-1111", "hashedPassword", MemberRole.INSTRUCTOR);
        Member savedInstructor = memberRepository.save(instructor);
        instructorId = savedInstructor.getId();
        instructorToken = jwtProvider.generateAccessToken(instructorId, instructor.getEmail(), instructor.getRole());
        
        // 테스트용 수강생 생성
        Member student = Member.create("수강생", "student@example.com", "010-2222-2222", "hashedPassword", MemberRole.STUDENT);
        Member savedStudent = memberRepository.save(student);
        studentId = savedStudent.getId();
        studentToken = jwtProvider.generateAccessToken(studentId, student.getEmail(), student.getRole());
    }

    @Test
    @DisplayName("강사가 강의를 성공적으로 개설한다")
    void createLecture_instructor_success() throws Exception {
        // Given
        CreateLectureRequest request = new CreateLectureRequest("내집마련 기초반", 10, 200000);
        String requestJson = objectMapper.writeValueAsString(request);
        
        // When & Then
        mockMvc.perform(post("/api/lectures")
                .header("Authorization", "Bearer " + instructorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value("내집마련 기초반"))
                .andExpect(jsonPath("$.data.maxCapacity").value(10))
                .andExpect(jsonPath("$.data.price").value(200000))
                .andExpect(jsonPath("$.data.instructorId").value(instructorId))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("수강생은 강의를 개설할 수 없다")
    void createLecture_student_fails() throws Exception {
        // Given
        CreateLectureRequest request = new CreateLectureRequest("내집마련 기초반", 10, 200000);
        String requestJson = objectMapper.writeValueAsString(request);
        
        // When & Then
        mockMvc.perform(post("/api/lectures")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_ROLE"));
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로는 강의를 개설할 수 없다")
    void createLecture_invalid_token_fails() throws Exception {
        // Given
        CreateLectureRequest request = new CreateLectureRequest("내집마련 기초반", 10, 200000);
        String requestJson = objectMapper.writeValueAsString(request);
        
        // When & Then
        mockMvc.perform(post("/api/lectures")
                .header("Authorization", "Bearer invalid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }

    @Test
    @DisplayName("Authorization 헤더 없이는 강의를 개설할 수 없다")
    void createLecture_no_auth_header_fails() throws Exception {
        // Given
        CreateLectureRequest request = new CreateLectureRequest("내집마련 기초반", 10, 200000);
        String requestJson = objectMapper.writeValueAsString(request);
        
        // When & Then
        mockMvc.perform(post("/api/lectures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }

    @Test
    @DisplayName("유효성 검증 실패 시 강의를 개설할 수 없다")
    void createLecture_validation_fails() throws Exception {
        // Given - 빈 제목
        CreateLectureRequest request = new CreateLectureRequest("", 10, 200000);
        String requestJson = objectMapper.writeValueAsString(request);
        
        // When & Then
        mockMvc.perform(post("/api/lectures")
                .header("Authorization", "Bearer " + instructorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("강의 정보를 성공적으로 조회한다")
    void getLecture_success() throws Exception {
        // Given - 먼저 강의를 생성
        CreateLectureRequest createRequest = new CreateLectureRequest("내집마련 기초반", 10, 200000);
        String createRequestJson = objectMapper.writeValueAsString(createRequest);
        
        String createResponse = mockMvc.perform(post("/api/lectures")
                .header("Authorization", "Bearer " + instructorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // JSON에서 생성된 강의 ID 추출
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(createResponse);
        Long lectureId = jsonNode.get("data").get("id").asLong();
        
        // When & Then
        mockMvc.perform(get("/api/lectures/" + lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(lectureId))
                .andExpect(jsonPath("$.data.title").value("내집마련 기초반"))
                .andExpect(jsonPath("$.data.maxCapacity").value(10))
                .andExpect(jsonPath("$.data.price").value(200000));
    }

    @Test
    @DisplayName("존재하지 않는 강의는 조회할 수 없다")
    void getLecture_not_found_fails() throws Exception {
        // Given
        Long nonExistentLectureId = 99999L;
        
        // When & Then
        mockMvc.perform(get("/api/lectures/" + nonExistentLectureId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("LECTURE_NOT_FOUND"));
    }
}
