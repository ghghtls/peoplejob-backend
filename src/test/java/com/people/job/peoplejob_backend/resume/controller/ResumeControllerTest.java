package com.people.job.resume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.resume.dto.ResumeDTO;
import com.people.job.resume.service.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("이력서 컨트롤러 테스트")
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeService resumeService;

    private ResumeDTO testResume;

    @BeforeEach
    void setUp() {
        testResume = ResumeDTO.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("백엔드 개발자 이력서")
                .name("홍길동")
                .email("hong@example.com")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .education("컴퓨터공학과 학사")
                .experience("Java/Spring 개발 3년")
                .skills("Java, Spring Boot, MySQL")
                .introduction("백엔드 개발에 열정이 있는 개발자입니다.")
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("이력서 목록 조회 테스트")
    void getResumeList() throws Exception {
        // Given
        List<ResumeDTO> resumeList = Arrays.asList(testResume);
        Page<ResumeDTO> resumePage = new PageImpl<>(resumeList, PageRequest.of(0, 10), 1);

        when(resumeService.findAll(any(), any())).thenReturn(resumePage);

        // When & Then
        mockMvc.perform(get("/api/resumes")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("백엔드 개발자 이력서"));
    }

    @Test
    @DisplayName("이력서 상세 조회 테스트")
    void getResumeDetail() throws Exception {
        // Given
        when(resumeService.findById(1L)).thenReturn(testResume);

        // When & Then
        mockMvc.perform(get("/api/resumes/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("백엔드 개발자 이력서"))
                .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    @DisplayName("이력서 등록 테스트")
    void createResume() throws Exception {
        // Given
        ResumeDTO newResume = ResumeDTO.builder()
                .userNo(1L)
                .title("프론트엔드 개발자 이력서")
                .name("김철수")
                .email("kim@example.com")
                .phone("010-9876-5432")
                .address("서울시 서초구")
                .education("컴퓨터공학과 학사")
                .experience("React 개발 2년")
                .skills("React, JavaScript, CSS")
                .introduction("사용자 경험을 중시하는 프론트엔드 개발자입니다.")
                .isPublic(true)
                .build();

        when(resumeService.save(any(ResumeDTO.class))).thenReturn(testResume);

        // When & Then
        mockMvc.perform(post("/api/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newResume)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("백엔드 개발자 이력서"));
    }

    @Test
    @DisplayName("이력서 수정 테스트")
    void updateResume() throws Exception {
        // Given
        ResumeDTO updatedResume = ResumeDTO.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("시니어 백엔드 개발자 이력서")
                .name("홍길동")
                .email("hong@example.com")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .education("컴퓨터공학과 학사")
                .experience("Java/Spring 개발 5년")
                .skills("Java, Spring Boot, MySQL, Redis")
                .introduction("시니어 백엔드 개발자로 성장하고 있습니다.")
                .isPublic(true)
                .build();

        when(resumeService.update(eq(1L), any(ResumeDTO.class))).thenReturn(updatedResume);

        // When & Then
        mockMvc.perform(put("/api/resumes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedResume)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("시니어 백엔드 개발자 이력서"));
    }

    @Test
    @DisplayName("이력서 삭제 테스트")
    void deleteResume() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/resumes/{id}", 1L))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("사용자별 이력서 조회 테스트")
    void getResumesByUser() throws Exception {
        // Given
        List<ResumeDTO> userResumes = Arrays.asList(testResume);
        Page<ResumeDTO> resumePage = new PageImpl<>(userResumes, PageRequest.of(0, 10), 1);

        when(resumeService.findByUserNo(eq(1L), any())).thenReturn(resumePage);

        // When & Then
        mockMvc.perform(get("/api/resumes/user/{userNo}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userNo").value(1L));
    }

    @Test
    @DisplayName("공개 이력서만 조회 테스트")
    void getPublicResumes() throws Exception {
        // Given
        List<ResumeDTO> publicResumes = Arrays.asList(testResume);
        Page<ResumeDTO> resumePage = new PageImpl<>(publicResumes, PageRequest.of(0, 10), 1);

        when(resumeService.findPublicResumes(any())).thenReturn(resumePage);

        // When & Then
        mockMvc.perform(get("/api/resumes/public")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].isPublic").value(true));
    }

    @Test
    @DisplayName("이력서 검색 테스트")
    void searchResumes() throws Exception {
        // Given
        List<ResumeDTO> searchResults = Arrays.asList(testResume);
        Page<ResumeDTO> searchPage = new PageImpl<>(searchResults, PageRequest.of(0, 10), 1);

        when(resumeService.search(eq("백엔드"), any())).thenReturn(searchPage);

        // When & Then
        mockMvc.perform(get("/api/resumes/search")
                        .param("keyword", "백엔드")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("백엔드 개발자 이력서"));
    }

    @Test
    @DisplayName("이력서 공개 상태 토글 테스트")
    void togglePublicStatus() throws Exception {
        // Given
        ResumeDTO privateResume = ResumeDTO.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("백엔드 개발자 이력서")
                .name("홍길동")
                .isPublic(false)
                .build();

        when(resumeService.togglePublicStatus(1L)).thenReturn(privateResume);

        // When & Then
        mockMvc.perform(patch("/api/resumes/{id}/toggle-public", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPublic").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 이력서 조회 시 404 에러 테스트")
    void getResumeNotFound() throws Exception {
        // Given
        when(resumeService.findById(999L))
                .thenThrow(new RuntimeException("이력서를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/resumes/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("잘못된 데이터로 이력서 등록 시 400 에러 테스트")
    void createResumeWithInvalidData() throws Exception {
        // Given
        ResumeDTO invalidResume = ResumeDTO.builder()
                .title("") // 빈 제목
                .name("홍길동")
                .build();

        // When & Then
        mockMvc.perform(post("/api/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidResume)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}