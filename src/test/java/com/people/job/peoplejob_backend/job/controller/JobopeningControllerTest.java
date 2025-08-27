package com.people.job.peoplejob_backend.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.job.controller.JobopeningController;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.service.JobopeningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobopeningController.class)
@DisplayName("채용공고 컨트롤러 테스트")
class JobopeningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobopeningService jobopeningService;

    private JobopeningDTO testJob;

    @BeforeEach
    void setUp() {
        testJob = JobopeningDTO.builder()
                .jobNo(1L)
                .title("백엔드 개발자 채용")
                .content("Spring Boot 기반 백엔드 개발자를 채용합니다.")
                .company("테스트 회사")
                .location("서울")
                .jobType("IT/소프트웨어")
                .salary("협의"
                        .workType("정규직")
                        .experience("3년 이상")
                        .education("학력무관")
                        .deadline(LocalDate.now().plusDays(30))
                        .regdate(LocalDate.now()) // LocalDate로 수정
                        .viewCount(0)
                        .isActive(true)
                        .userNo(1L)
                        .status("DRAFT")
                        .build();
    }

    @Test
    @DisplayName("채용공고 생성 성공 테스트")
    void createJobSuccess() throws Exception {
        // Given
        when(jobopeningService.create(any(JobopeningDTO.class))).thenReturn(testJob);

        // When & Then
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testJob)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("채용공고가 생성되었습니다."))
                .andExpect(jsonPath("$.job.title").value("백엔드 개발자 채용"));
    }

    @Test
    @DisplayName("채용공고 목록 조회 성공 테스트")
    void getAllJobsSuccess() throws Exception {
        // Given
        List<JobopeningDTO> jobs = Arrays.asList(testJob);
        Page<JobopeningDTO> jobPage = new PageImpl<>(jobs, PageRequest.of(0, 10), 1);

        when(jobopeningService.getAll(any(Pageable.class))).thenReturn(jobPage);

        // When & Then
        mockMvc.perform(get("/api/jobs")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("백엔드 개발자 채용"));
    }

    @Test
    @DisplayName("게시중인 채용공고 목록 조회 성공 테스트")
    void getPublishedJobsSuccess() throws Exception {
        // Given
        testJob.setStatus("PUBLISHED");
        List<JobopeningDTO> jobs = Arrays.asList(testJob);
        Page<JobopeningDTO> jobPage = new PageImpl<>(jobs, PageRequest.of(0, 10), 1);

        when(jobopeningService.getPublishedJobs(any(Pageable.class))).thenReturn(jobPage);

        // When & Then
        mockMvc.perform(get("/api/jobs")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "published"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("채용공고 상세 조회 성공 테스트")
    void getJobByIdSuccess() throws Exception {
        // Given
        when(jobopeningService.getById(1L)).thenReturn(testJob);

        // When & Then
        mockMvc.perform(get("/api/jobs/{jobNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobNo").value(1))
                .andExpect(jsonPath("$.title").value("백엔드 개발자 채용"));
    }

    @Test
    @DisplayName("채용공고 수정 성공 테스트")
    void updateJobSuccess() throws Exception {
        // Given
        testJob.setTitle("수정된 채용공고 제목");
        when(jobopeningService.update(eq(1L), any(JobopeningDTO.class))).thenReturn(testJob);

        // When & Then
        mockMvc.perform(put("/api/jobs/{jobNo}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testJob)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("채용공고가 수정되었습니다."))
                .andExpect(jsonPath("$.job.title").value("수정된 채용공고 제목"));
    }

    @Test
    @DisplayName("채용공고 삭제 성공 테스트")
    void deleteJobSuccess() throws Exception {
        // Given
        doNothing().when(jobopeningService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/jobs/{jobNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("채용공고가 삭제되었습니다."));
    }

    @Test
    @DisplayName("채용공고 임시저장 성공 테스트")
    void saveDraftSuccess() throws Exception {
        // Given
        testJob.setStatus("DRAFT");
        when(jobopeningService.saveDraft(any(JobopeningDTO.class))).thenReturn(testJob);

        // When & Then
        mockMvc.perform(post("/api/jobs/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testJob)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("채용공고가 임시저장되었습니다."))
                .andExpect(jsonPath("$.job.status").value("DRAFT"));
    }

    @Test
    @DisplayName("사용자별 임시저장 목록 조회 성공 테스트")
    void getUserDraftsSuccess() throws Exception {
        // Given
        List<JobopeningDTO> drafts = Arrays.asList(testJob);
        Page<JobopeningDTO> draftPage = new PageImpl<>(drafts, PageRequest.of(0, 10), 1);

        when(jobopeningService.getDraftsByUser(eq(1L), any(Pageable.class))).thenReturn(draftPage);

        // When & Then
        mockMvc.perform(get("/api/jobs/user/{userNo}/drafts", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("채용공고 게시 성공 테스트")
    void publishJobSuccess() throws Exception {
        // Given
        testJob.setStatus("PUBLISHED");
        when(jobopeningService.publish(eq(1L), eq(1L))).thenReturn(testJob);

        // When & Then
        mockMvc.perform(post("/api/jobs/{jobNo}/publish", 1L)
                        .param("userNo", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.message").value("채용공고가 게시되었습니다."))
                .andExpected(jsonPath("$.job.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("사용자별 채용공고 목록 조회 성공 테스트")
    void getUserJobsSuccess() throws Exception {
        // Given
        List<JobopeningDTO> jobs = Arrays.asList(testJob);
        Page<JobopeningDTO> jobPage = new PageImpl<>(jobs, PageRequest.of(0, 10), 1);

        when(jobopeningService.getByUser(eq(1L), any(Pageable.class))).thenReturn(jobPage);

        // When & Then
        mockMvc.perform(get("/api/jobs/user/{userNo}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("사용자 채용공고 상태별 개수 조회 성공 테스트")
    void getUserJobStatusCountsSuccess() throws Exception {
        // Given
        Map<String, Long> counts = new HashMap<>();
        counts.put("DRAFT", 5L);
        counts.put("PUBLISHED", 10L);
        counts.put("EXPIRED", 3L);

        when(jobopeningService.getJobStatusCounts(1L)).thenReturn(counts);

        // When & Then
        mockMvc.perform(get("/api/jobs/user/{userNo}/status-counts", 1L))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.DRAFT").value(5))
                .andExpected(jsonPath("$.PUBLISHED").value(10))
                .andExpected(jsonPath("$.EXPIRED").value(3));
    }

    @Test
    @DisplayName("채용공고 검색 성공 테스트")
    void searchJobsSuccess() throws Exception {
        // Given
        List<JobopeningDTO> jobs = Arrays.asList(testJob);
        Page<JobopeningDTO> jobPage = new PageImpl<>(jobs, PageRequest.of(0, 10), 1);

        when(jobopeningService.searchJobs(eq("백엔드"), any(Pageable.class))).thenReturn(jobPage);

        // When & Then
        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "백엔드")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.content").isArray())
                .andExpected(jsonPath("$.content[0].title").value("백엔드 개발자 채용"));
    }

    @Test
    @DisplayName("채용공고 카테고리별 조회 성공 테스트")
    void getJobsByCategorySuccess() throws Exception {
        // Given
        List<JobopeningDTO> jobs = Arrays.asList(testJob);
        Page<JobopeningDTO> jobPage = new PageImpl<>(jobs, PageRequest.of(0, 10), 1);

        when(jobopeningService.getJobsByCategory(eq("IT/소프트웨어"), eq("서울"), any(Pageable.class)))
                .thenReturn(jobPage);

        // When & Then
        mockMvc.perform(get("/api/jobs/category")
                        .param("jobType", "IT/소프트웨어")
                        .param("location", "서울")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("채용공고 마감 처리 성공 테스트")
    void expireJobSuccess() throws Exception {
        // Given
        testJob.setStatus("EXPIRED");
        when(jobopeningService.changeStatus(eq(1L), eq("EXPIRED"), isNull())).thenReturn(testJob);

        // When & Then
        mockMvc.perform(post("/api/jobs/{jobNo}/expire", 1L))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.message").value("채용공고가 마감 처리되었습니다."));
    }

    @Test
    @DisplayName("마감일 지난 채용공고 처리 성공 테스트")
    void expireOverdueJobsSuccess() throws Exception {
        // Given
        doNothing().when(jobopeningService).expireOverdueJobs();

        // When & Then
        mockMvc.perform(post("/api/jobs/expire-overdue"))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.message").value("마감일이 지난 채용공고들이 처리되었습니다."));
    }
}