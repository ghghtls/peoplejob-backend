package com.people.job.peoplejob_backend.apply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.apply.controller.ApplyController;
import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.service.ApplyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplyController.class)
@DisplayName("지원 컨트롤러 테스트")
class ApplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplyService applyService;

    private ApplyDTO testApply;

    @BeforeEach
    void setUp() {
        testApply = ApplyDTO.builder()
                .applyNo(1L)
                .jobNo(1L) // jobopeningNo -> jobNo로 수정
                .userNo(1L) // 추가
                .resumeNo(1L)
                .applyDate(LocalDate.now()) // regdate -> applyDate로 수정
                .status("PENDING") // 추가
                .message("지원합니다.") // 추가
                .jobTitle("백엔드 개발자") // 추가 정보
                .companyName("테스트 회사") // 추가 정보
                .build();
    }

    @Test
    @DisplayName("지원하기 성공 테스트")
    void applySuccess() throws Exception {
        // Given
        when(applyService.apply(any(ApplyDTO.class))).thenReturn(testApply);

        // When & Then
        mockMvc.perform(post("/api/applies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testApply)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원이 완료되었습니다."))
                .andExpect(jsonPath("$.apply.status").value("PENDING"));
    }

    @Test
    @DisplayName("지원 취소 성공 테스트")
    void cancelApplySuccess() throws Exception {
        // Given
        doNothing().when(applyService).cancel(1L);

        // When & Then
        mockMvc.perform(delete("/api/applies/{applyNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원이 취소되었습니다."));
    }

    @Test
    @DisplayName("사용자별 지원 내역 조회 성공 테스트")
    void getUserAppliesSuccess() throws Exception {
        // Given
        List<ApplyDTO> applies = Arrays.asList(testApply);
        Page<ApplyDTO> applyPage = new PageImpl<>(applies, PageRequest.of(0, 10), 1);

        when(applyService.getUserApplies(eq(1L), any(Pageable.class))).thenReturn(applyPage);

        // When & Then
        mockMvc.perform(get("/api/applies/user/{userNo}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].applyNo").value(1));
    }

    @Test
    @DisplayName("채용공고별 지원자 목록 조회 성공 테스트")
    void getJobApplicantsSuccess() throws Exception {
        // Given
        List<ApplyDTO> applicants = Arrays.asList(testApply);
        Page<ApplyDTO> applicantPage = new PageImpl<>(applicants, PageRequest.of(0, 10), 1);

        when(applyService.getJobApplicants(eq(1L), any(Pageable.class))).thenReturn(applicantPage);

        // When & Then
        mockMvc.perform(get("/api/applies/job/{jobNo}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("지원 상태 변경 성공 테스트")
    void updateApplyStatusSuccess() throws Exception {
        // Given
        testApply.setStatus("ACCEPTED");
        when(applyService.updateStatus(eq(1L), eq("ACCEPTED"), anyString())).thenReturn(testApply);

        // When & Then
        mockMvc.perform(put("/api/applies/{applyNo}/status", 1L)
                        .param("status", "ACCEPTED")
                        .param("note", "합격입니다."))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원 상태가 변경되었습니다."))
                .andExpect(jsonPath("$.apply.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("지원 상세 조회 성공 테스트")
    void getApplyDetailSuccess() throws Exception {
        // Given
        when(applyService.getById(1L)).thenReturn(testApply);

        // When & Then
        mockMvc.perform(get("/api/applies/{applyNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applyNo").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("중복 지원 체크 테스트")
    void checkDuplicateApplySuccess() throws Exception {
        // Given
        when(applyService.isAlreadyApplied(1L, 1L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/applies/check")
                        .param("userNo", "1")
                        .param("jobNo", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canApply").value(true));
    }

    @Test
    @DisplayName("사용자 지원 통계 조회 성공 테스트")
    void getUserApplyStatsSuccess() throws Exception {
        // Given
        when(applyService.getUserApplyStats(1L)).thenReturn(Map.of(
                "total", 10L,
                "pending", 5L,
                "accepted", 3L,
                "rejected", 2L
        ));

        // When & Then
        mockMvc.perform(get("/api/applies/user/{userNo}/stats", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.pending").value(5));
    }
}