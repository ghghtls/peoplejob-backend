package com.people.job.peoplejob_backend.inquiry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.inquiry.controller.InquiryController;
import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.inquiry.service.InquiryService;
import com.people.job.user.security.JwtTokenProvider;
import com.people.job.user.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InquiryController.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("문의사항 컨트롤러 테스트")
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InquiryService inquiryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private InquiryDTO testInquiry;

    @BeforeEach
    void setUp() {
        testInquiry = InquiryDTO.builder()
                .inquiryNo(1L)
                .title("서비스 이용 문의")
                .content("서비스 이용 방법에 대해 문의드립니다.")
                .writer("홍길동")
                .email("hong@example.com")
                .phone("010-1234-5678")
                .category("서비스")
                .regdate(LocalDate.now())
                .isAnswered(false)
                .answer(null)
                .answerDate(null)
                .answerBy(null)
                .build();
    }

    @Test
    @DisplayName("문의사항 등록 성공 테스트")
    void insertInquirySuccess() throws Exception {
        // Given
        doNothing().when(inquiryService).insertInquiry(any(InquiryDTO.class)); // 실제 메서드명

        // When & Then
        mockMvc.perform(post("/api/inquiry") // 실제 매핑 경로
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInquiry)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("문의가 등록되었습니다.")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("전체 문의사항 조회 성공 테스트")
    void getAllInquiriesSuccess() throws Exception {
        // Given
        List<InquiryDTO> inquiries = Arrays.asList(testInquiry);
        when(inquiryService.getAllInquiries()).thenReturn(inquiries);

        // When & Then
        mockMvc.perform(get("/api/inquiry"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("서비스 이용 문의"));
    }

    @Test
    @DisplayName("문의사항 상세 조회 성공 테스트")
    void getInquiryDetailSuccess() throws Exception {
        // Given
        when(inquiryService.getInquiry(1L)).thenReturn(testInquiry); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/inquiry/{inquiryNo}", 1L)) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inquiryNo").value(1))
                .andExpect(jsonPath("$.title").value("서비스 이용 문의"))
                .andExpect(jsonPath("$.isAnswered").value(false));
    }

    @Test
    @DisplayName("문의사항 수정 성공 테스트")
    void updateInquirySuccess() throws Exception {
        // Given
        InquiryDTO updateInquiry = InquiryDTO.builder()
                .inquiryNo(1L)
                .title("수정된 문의 제목")
                .content("수정된 문의 내용")
                .build();

        doNothing().when(inquiryService).updateInquiry(any(InquiryDTO.class)); // 실제 메서드명

        // When & Then
        mockMvc.perform(put("/api/inquiry/{inquiryNo}", 1L) // 실제 매핑 경로
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateInquiry)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("문의 수정 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("문의사항 삭제 성공 테스트")
    void deleteInquirySuccess() throws Exception {
        // Given
        doNothing().when(inquiryService).deleteInquiry(1L); // 실제 메서드명

        // When & Then
        mockMvc.perform(delete("/api/inquiry/{inquiryNo}", 1L) // 실제 매핑 경로
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("문의 삭제 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("문의사항 답변 등록 성공 테스트")
    void answerInquirySuccess() throws Exception {
        // Given
        doNothing().when(inquiryService).answerInquiry(eq(1L), anyString()); // 실제 메서드명

        // When & Then
        mockMvc.perform(put("/api/inquiry/{inquiryNo}/answer", 1L) // 실제 매핑 경로
                        .with(csrf())
                        .param("answer", "문의해 주신 내용에 대해 답변드립니다."))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("답변 등록 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("문의사항 등록 실패 테스트 - 필수 정보 누락")
    void insertInquiryFailMissingInfo() throws Exception {
        // Given
        InquiryDTO invalidInquiry = InquiryDTO.builder()
                .title("") // 빈 제목
                .content("내용")
                .build();

        doThrow(new RuntimeException("제목은 필수입니다."))
                .when(inquiryService).insertInquiry(any(InquiryDTO.class));

        // When & Then
        mockMvc.perform(post("/api/inquiry")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInquiry)))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // RuntimeException으로 500 에러
    }

    @Test
    @DisplayName("존재하지 않는 문의사항 조회 테스트")
    void getInquiryDetailNotFound() throws Exception {
        // Given
        when(inquiryService.getInquiry(999L))
                .thenThrow(new RuntimeException("문의사항을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/inquiry/{inquiryNo}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // RuntimeException으로 500 에러
    }

    @Test
    @DisplayName("존재하지 않는 문의사항 수정 테스트")
    void updateInquiryNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("문의사항을 찾을 수 없습니다."))
                .when(inquiryService).updateInquiry(any(InquiryDTO.class));

        // When & Then
        mockMvc.perform(put("/api/inquiry/{inquiryNo}", 999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInquiry)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 문의사항 삭제 테스트")
    void deleteInquiryNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("문의사항을 찾을 수 없습니다."))
                .when(inquiryService).deleteInquiry(999L);

        // When & Then
        mockMvc.perform(delete("/api/inquiry/{inquiryNo}", 999L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}