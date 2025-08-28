package com.people.job.peoplejob_backend.resume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.resume.controller.ResumeController;
import com.people.job.resume.dto.ResumeDTO;
import com.people.job.resume.service.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeController.class)
@ActiveProfiles("test")
@DisplayName("이력서 컨트롤러 테스트")
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ResumeService resumeService;

    private ResumeDTO testResume;

    @BeforeEach
    void setUp() {
        testResume = ResumeDTO.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("백엔드 개발자 이력서")
                .content("상세한 이력서 내용입니다.")
                .education("컴퓨터공학과 학사")
                .career("Java/Spring 개발 3년") // career 필드
                .certificate("정보처리기사") // certificate 필드
                .hopeJobtype("IT/소프트웨어") // hopeJobtype 필드
                .hopeLocation("서울") // hopeLocation 필드
                .salary("3000만원~4000만원") // salary 필드
                .workType("정규직") // workType 필드
                .regdate(LocalDate.now()) // regdate 필드
                .imagePath(null)
                .originalImage(null)
                .build();
    }

    @Test
    @DisplayName("이력서 등록 성공 테스트")
    void insertResumeSuccess() throws Exception {
        // Given
        when(resumeService.insertResume(any(ResumeDTO.class))).thenReturn(1L); // 실제 메서드명

        // When & Then
        mockMvc.perform(post("/api/resume") // 실제 매핑 경로
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testResume)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이력서 등록 완료")) // 실제 응답 메시지
                .andExpect(jsonPath("$.resumeId").value(1));
    }

    @Test
    @DisplayName("전체 이력서 조회 성공 테스트")
    void selectAllResumesSuccess() throws Exception {
        // Given
        List<ResumeDTO> resumes = Arrays.asList(testResume);
        when(resumeService.selectAll()).thenReturn(resumes); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/resume")) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("백엔드 개발자 이력서"));
    }

    @Test
    @DisplayName("이력서 ID로 조회 성공 테스트")
    void selectResumeByNoSuccess() throws Exception {
        // Given
        when(resumeService.selectByNo(1L)).thenReturn(testResume); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/resume/{id}", 1L)) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeNo").value(1))
                .andExpect(jsonPath("$.title").value("백엔드 개발자 이력서"))
                .andExpect(jsonPath("$.career").value("Java/Spring 개발 3년"));
    }

    @Test
    @DisplayName("이력서 수정 성공 테스트")
    void updateResumeSuccess() throws Exception {
        // Given
        ResumeDTO updateResume = ResumeDTO.builder()
                .resumeNo(1L)
                .title("시니어 백엔드 개발자 이력서")
                .content("수정된 이력서 내용입니다.")
                .career("Java/Spring 개발 5년")
                .certificate("정보처리기사, AWS 자격증")
                .build();

        doNothing().when(resumeService).updateResume(any(ResumeDTO.class)); // 실제 메서드명

        // When & Then
        mockMvc.perform(put("/api/resume/{id}", 1L) // 실제 매핑 경로
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateResume)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("이력서 수정 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("이력서 삭제 성공 테스트")
    void deleteResumeSuccess() throws Exception {
        // Given
        doNothing().when(resumeService).deleteResume(1L); // 실제 메서드명

        // When & Then
        mockMvc.perform(delete("/api/resume/{id}", 1L)) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("이력서 삭제 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("사용자별 이력서 조회 성공 테스트")
    void selectResumesByUserSuccess() throws Exception {
        // Given
        List<ResumeDTO> resumes = Arrays.asList(testResume);
        when(resumeService.selectByUserNo(1L)).thenReturn(resumes); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/resume/user/{userNo}", 1L)) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userNo").value(1))
                .andExpect(jsonPath("$[0].title").value("백엔드 개발자 이력서"));
    }

    @Test
    @DisplayName("이력서 등록 실패 테스트 - 필수 정보 누락")
    void insertResumeFailMissingInfo() throws Exception {
        // Given
        ResumeDTO invalidResume = ResumeDTO.builder()
                .title("") // 빈 제목
                .content("내용")
                .build();

        when(resumeService.insertResume(any(ResumeDTO.class)))
                .thenThrow(new RuntimeException("제목은 필수입니다."));

        // When & Then
        mockMvc.perform(post("/api/resume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidResume)))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // RuntimeException으로 500 에러
    }

    @Test
    @DisplayName("존재하지 않는 이력서 조회 테스트")
    void selectResumeByNoNotFound() throws Exception {
        // Given
        when(resumeService.selectByNo(999L))
                .thenThrow(new RuntimeException("이력서를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/resume/{id}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 이력서 수정 테스트")
    void updateResumeNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("이력서를 찾을 수 없습니다."))
                .when(resumeService).updateResume(any(ResumeDTO.class));

        // When & Then
        mockMvc.perform(put("/api/resume/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testResume)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 이력서 삭제 테스트")
    void deleteResumeNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("이력서를 찾을 수 없습니다."))
                .when(resumeService).deleteResume(999L);

        // When & Then
        mockMvc.perform(delete("/api/resume/{id}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("사용자별 이력서 조회 - 빈 목록 테스트")
    void selectResumesByUserEmpty() throws Exception {
        // Given
        List<ResumeDTO> emptyList = Arrays.asList();
        when(resumeService.selectByUserNo(1L)).thenReturn(emptyList);

        // When & Then
        mockMvc.perform(get("/api/resume/user/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}