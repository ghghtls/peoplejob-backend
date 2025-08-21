package com.people.job.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.service.JobopeningService;
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

import java.time.LocalDate;
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
@DisplayName("채용공고 컨트롤러 테스트")
class JobopeningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobopeningService jobopeningService;

    private JobopeningDTO testJob;

    @BeforeEach
    void setUp() {
        testJob = JobopeningDTO.builder()
                .jobNo(1L)
                .title("백엔드 개발자 모집")
                .content("Java/Spring Boot 개발자를 모집합니다.")
                .company("테스트회사")
                .location("서울시 강남구")
                .jobType("정규직")
                .salary("협의")
                .workType("오프라인")
                .experience("경력무관")
                .education("대졸")
                .deadline(LocalDate.now().plusDays(30))
                .regdate(LocalDateTime.now())
                .viewCount(0)
                .isActive(true)
                .userNo(1L)
                .status("PUBLISHED")
                .build();
    }

    @Test
    @DisplayName("채용공고 목록 조회 테스트")
    void getJobList() throws Exception {
        // Given
        List<JobopeningDTO> jobList = Arrays.asList(testJob);
        Page<JobopeningDTO> jobPage = new PageImpl<>(jobList, PageRequest.of(0, 10), 1);

        when(jobopeningService.findAll(any(), any())).thenReturn(jobPage);

        // When & Then
        mockMvc.perform(get("/api/jobs")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("백엔드 개발자 모집"));
    }

    @Test
    @DisplayName("채용공고 상세 조회 테스트")
    void getJobDetail() throws Exception {
        // Given
        when(jobopeningService.findById(1L)).thenReturn(testJob);

        // When & Then
        mockMvc.perform(get("/api/jobs/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("백엔드 개발자 모집"))
                .andExpect(jsonPath("$.company").value("테스트회사"));
    }

    @Test
    @DisplayName("채용공고 등록 테스트")
    void createJob() throws Exception {
        // Given
        JobopeningDTO newJob = JobopeningDTO.builder()
                .title("프론트엔드 개발자 모집")
                .content("React 개발자를 모집합니다.")
                .company("신규회사")
                .location("서울시 서초구")
                .jobType("정규직")
                .salary("3000만원")
                .workType("하이브리드")
                .experience("3년 이상")
                .education("대졸")
                .deadline(LocalDate.now().plusDays(30))
                .userNo(1L)
                .build();

        when(jobopeningService.save(any(JobopeningDTO.class))).thenReturn(testJob);

        // When & Then
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newJob)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("백엔드 개발자 모집"));
    }

    @Test
    @DisplayName("채용공고 수정 테스트")
    void updateJob() throws Exception {
        // Given
        JobopeningDTO updatedJob = JobopeningDTO.builder()
                .jobNo(1L)
                .title("시니어 백엔드 개발자 모집")
                .content("경험 많은 Java/Spring Boot 개발자를 모집합니다.")
                .company("테스트회사")
                .location("서울시 강남구")
                .jobType("정규직")
                .salary("5000만원")
                .workType("하이브리드")
                .experience("5년 이상")
                .education("대졸")
                .deadline(LocalDate.now().plusDays(30))
                .userNo(1L)
                .build();

        when(jobopeningService.update(eq(1L), any(JobopeningDTO.class))).thenReturn(updatedJob);

        // When & Then
        mockMvc.perform(put("/api/jobs/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedJob)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("시니어 백엔드 개발자 모집"));
    }

    @Test
    @DisplayName("채용공고 삭제 테스트")
    void deleteJob() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/jobs/{id}", 1L))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("회사별 채용공고 조회 테스트")
    void getJobsByCompany() throws Exception {
        // Given
        List<JobopeningDTO> jobList = Arrays.asList(testJob);
        Page<JobopeningDTO> jobPage = new PageImpl<>(jobList, PageRequest.of(0, 10), 1);

        when(jobopeningService.findByCompany(eq("테스트회사"), any())).thenReturn(jobPage);

        // When & Then
        mockMvc.perform(get("/api/jobs/company/{company}", "테스트회사")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].company").value("테스트회사"));
    }

    @Test
    @DisplayName("채용공고 검색 테스트")
    void searchJobs() throws Exception {
        // Given
        List<JobopeningDTO> searchResults = Arrays.asList(testJob);
        Page<JobopeningDTO> searchPage = new PageImpl<>(searchResults, PageRequest.of(0, 10), 1);

        when(jobopeningService.search(eq("백엔드"), any())).thenReturn(searchPage);

        // When & Then
        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "백엔드")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("백엔드 개발자 모집"));
    }

    @Test
    @DisplayName("채용공고 필터링 테스트")
    void filterJobs() throws Exception {
        // Given
        List<JobopeningDTO> filteredResults = Arrays.asList(testJob);
        Page<JobopeningDTO> filteredPage = new PageImpl<>(filteredResults, PageRequest.of(0, 10), 1);

        when(jobopeningService.findByFilters(any(), any(), any(), any(), any())).thenReturn(filteredPage);

        // When & Then
        mockMvc.perform(get("/api/jobs/filter")
                        .param("location", "서울시 강남구")
                        .param("jobType", "정규직")
                        .param("experience", "경력무관")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].location").value("서울시 강남구"));
    }

    @Test
    @DisplayName("존재하지 않는 채용공고 조회 시 404 에러 테스트")
    void getJobNotFound() throws Exception {
        // Given
        when(jobopeningService.findById(999L))
                .thenThrow(new RuntimeException("채용공고를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/jobs/{id}", 999L))
                .andDo(print())
                .andExpected(status().isNotFound());
    }

    @Test
    @DisplayName("잘못된 데이터로 채용공고 등록 시 400 에러 테스트")
    void createJobWithInvalidData() throws Exception {
        // Given
        JobopeningDTO invalidJob = JobopeningDTO.builder()
                .title("") // 빈 제목
                .content("내용")
                .build();

        // When & Then
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidJob)))
                .andDo(print())
                .andExpected(status().isBadRequest());
    }
}