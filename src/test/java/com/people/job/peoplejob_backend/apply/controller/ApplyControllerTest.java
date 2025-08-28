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

@WebMvcTest(ApplyController.class)
@ActiveProfiles("test")
@DisplayName("지원 컨트롤러 테스트")
class ApplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplyService applyService;

    private ApplyDTO testApply;

    @BeforeEach
    void setUp() {
        testApply = ApplyDTO.builder()
                .applyNo(1L)
                .jobNo(1L) // 실제 DB 스키마와 일치
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDate.now()) // 실제 DB 스키마와 일치
                .status("PENDING")
                .message("지원합니다.")
                .build();
    }

    @Test
    @DisplayName("지원하기 성공 테스트")
    void applySuccess() throws Exception {
        // Given
        doNothing().when(applyService).applyToJob(any(ApplyDTO.class));

        // When & Then
        mockMvc.perform(post("/api/apply") // 실제 컨트롤러 매핑과 일치
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testApply)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("지원 완료!")); // 실제 응답 메시지와 일치
    }

    @Test
    @DisplayName("지원 실패 테스트 - 중복 지원")
    void applyFailDuplicate() throws Exception {
        // Given
        doThrow(new RuntimeException("이미 지원한 공고입니다."))
                .when(applyService).applyToJob(any(ApplyDTO.class));

        // When & Then
        mockMvc.perform(post("/api/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testApply)))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // RuntimeException 발생 시 500 에러
    }

    @Test
    @DisplayName("이력서별 지원 내역 조회 성공 테스트")
    void getAppliesByResumeSuccess() throws Exception {
        // Given
        List<ApplyDTO> applies = Arrays.asList(testApply);
        when(applyService.getAppliesByResume(1L)).thenReturn(applies);

        // When & Then
        mockMvc.perform(get("/api/apply/resume/{resumeNo}", 1L)) // 실제 매핑과 일치
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].resumeNo").value(1));
    }

    @Test
    @DisplayName("채용공고별 지원자 목록 조회 성공 테스트")
    void getAppliesByJobSuccess() throws Exception {
        // Given
        List<ApplyDTO> applicants = Arrays.asList(testApply);
        when(applyService.getAppliesByJobopening(1L)).thenReturn(applicants); // 실제 메서드명과 일치

        // When & Then
        mockMvc.perform(get("/api/apply/job/{jobopeningNo}", 1L)) // 실제 매핑과 일치
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].jobNo").value(1));
    }

    @Test
    @DisplayName("지원 취소 성공 테스트")
    void cancelApplySuccess() throws Exception {
        // Given
        doNothing().when(applyService).cancelApply(1L);

        // When & Then
        mockMvc.perform(delete("/api/apply/{applyNo}", 1L)) // 실제 매핑과 일치
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("지원 취소 완료")); // 실제 응답 메시지와 일치
    }

    @Test
    @DisplayName("존재하지 않는 지원서 취소 테스트")
    void cancelApplyNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("지원 내역을 찾을 수 없습니다."))
                .when(applyService).cancelApply(999L);

        // When & Then
        mockMvc.perform(delete("/api/apply/{applyNo}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}