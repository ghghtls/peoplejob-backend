package com.people.job.apply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.service.ApplyService;
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
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDateTime.now())
                .status("PENDING")
                .coverLetter("지원 동기입니다.")
                .build();
    }

    @Test
    @DisplayName("지원 목록 조회 테스트")
    void getApplyList() throws Exception {
        // Given
        List<ApplyDTO> applyList = Arrays.asList(testApply);
        Page<ApplyDTO> applyPage = new PageImpl<>(applyList, PageRequest.of(0, 10), 1);

        when(applyService.findAll(any(), any())).thenReturn(applyPage);

        // When & Then
        mockMvc.perform(get("/api/applies")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("지원 상세 조회 테스트")
    void getApplyDetail() throws Exception {
        // Given
        when(applyService.findById(1L)).thenReturn(testApply);

        // When & Then
        mockMvc.perform(get("/api/applies/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applyNo").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("지원서 제출 테스트")
    void submitApply() throws Exception {
        // Given
        ApplyDTO newApply = ApplyDTO.builder()
                .jobNo(2L)
                .userNo(1L)
                .resumeNo(1L)
                .coverLetter("새로운 지원 동기입니다.")
                .build();

        when(applyService.submit(any(ApplyDTO.class))).thenReturn(testApply);

        // When & Then
        mockMvc.perform(post("/api/applies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newApply)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("지원서 취소 테스트")
    void cancelApply() throws Exception {
        // Given
        ApplyDTO cancelledApply = ApplyDTO.builder()
                .applyNo(1L)
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDateTime.now())
                .status("CANCELLED")
                .coverLetter("지원 동기입니다.")
                .build();

        when(applyService.cancel(1L)).thenReturn(cancelledApply);

        // When & Then
        mockMvc.perform(patch("/api/applies/{id}/cancel", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("사용자별 지원 목록 조회 테스트")
    void getAppliesByUser() throws Exception {
        // Given
        List<ApplyDTO> userApplies = Arrays.asList(testApply);
        Page<ApplyDTO> applyPage = new PageImpl<>(userApplies, PageRequest.of(0, 10), 1);

        when(applyService.findByUserNo(eq(1L), any())).thenReturn(applyPage);

        // When & Then
        mockMvc.perform(get("/api/applies/user/{userNo}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userNo").value(1L));
    }

    @Test
    @DisplayName("채용공고별 지원 목록 조회 테스트")
    void getAppliesByJob() throws Exception {
        // Given
        List<ApplyDTO> jobApplies = Arrays.asList(testApply);
        Page<ApplyDTO> applyPage = new PageImpl<>(jobApplies, PageRequest.of(0, 10), 1);

        when(applyService.findByJobNo(eq(1L), any())).thenReturn(applyPage);

        // When & Then
        mockMvc.perform(get("/api/applies/job/{jobNo}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].jobNo").value(1L));
    }

    @Test
    @DisplayName("지원 상태 변경 테스트")
    void updateApplyStatus() throws Exception {
        // Given
        ApplyDTO updatedApply = ApplyDTO.builder()
                .applyNo(1L)
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDateTime.now())
                .status("ACCEPTED")
                .coverLetter("지원 동기입니다.")
                .build();

        when(applyService.updateStatus(eq(1L), eq("ACCEPTED"))).thenReturn(updatedApply);

        // When & Then
        mockMvc.perform(patch("/api/applies/{id}/status", 1L)
                        .param("status", "ACCEPTED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("존재하지 않는 지원서 조회 시 404 에러 테스트")
    void getApplyNotFound() throws Exception {
        // Given
        when(applyService.findById(999L))
                .thenThrow(new RuntimeException("지원서를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/applies/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("중복 지원 시 400 에러 테스트")
    void duplicateApply() throws Exception {
        // Given
        ApplyDTO duplicateApply = ApplyDTO.builder()
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .coverLetter("중복 지원")
                .build();

        when(applyService.submit(any(ApplyDTO.class)))
                .thenThrow(new IllegalArgumentException("이미 지원한 채용공고입니다."));

        // When & Then
        mockMvc.perform(post("/api/applies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateApply)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}