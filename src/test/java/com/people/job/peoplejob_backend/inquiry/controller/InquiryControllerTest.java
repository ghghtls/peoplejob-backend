package com.people.job.inquiry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.inquiry.service.InquiryService;
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
@DisplayName("문의 컨트롤러 테스트")
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InquiryService inquiryService;

    private InquiryDTO testInquiry;

    @BeforeEach
    void setUp() {
        testInquiry = InquiryDTO.builder()
                .inquiryNo(1L)
                .userNo(1L)
                .title("서비스 이용 문의")
                .content("서비스 이용에 대해 문의드립니다.")
                .category("GENERAL")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("문의 목록 조회 테스트")
    void getInquiryList() throws Exception {
        // Given
        List<InquiryDTO> inquiryList = Arrays.asList(testInquiry);
        Page<InquiryDTO> inquiryPage = new PageImpl<>(inquiryList, PageRequest.of(0, 10), 1);

        when(inquiryService.findAll(any(), any())).thenReturn(inquiryPage);

        // When & Then
        mockMvc.perform(get("/api/inquiries")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("서비스 이용 문의"));
    }

    @Test
    @DisplayName("문의 등록 테스트")
    void createInquiry() throws Exception {
        // Given
        InquiryDTO newInquiry = InquiryDTO.builder()
                .userNo(1L)
                .title("결제 관련 문의")
                .content("결제가 정상적으로 처리되지 않습니다.")
                .category("PAYMENT")
                .build();

        when(inquiryService.save(any(InquiryDTO.class))).thenReturn(testInquiry);

        // When & Then
        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newInquiry)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("서비스 이용 문의"));
    }

    @Test
    @DisplayName("문의 답변 등록 테스트")
    void answerInquiry() throws Exception {
        // Given
        InquiryDTO answeredInquiry = InquiryDTO.builder()
                .inquiryNo(1L)
                .userNo(1L)
                .title("서비스 이용 문의")
                .content("서비스 이용에 대해 문의드립니다.")
                .category("GENERAL")
                .status("ANSWERED")
                .answer("문의해주셔서 감사합니다. 답변드립니다.")
                .answeredAt(LocalDateTime.now())
                .answeredBy("관리자")
                .createdAt(LocalDateTime.now())
                .build();

        when(inquiryService.answerInquiry(eq(1L), any(String.class))).thenReturn(answeredInquiry);

        // When & Then
        mockMvc.perform(patch("/api/inquiries/{id}/answer", 1L)
                        .param("answer", "문의해주셔서 감사합니다. 답변드립니다."))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANSWERED"))
                .andExpect(jsonPath("$.answer").value("문의해주셔서 감사합니다. 답변드립니다."));
    }

    @Test
    @DisplayName("사용자별 문의 목록 조회 테스트")
    void getInquiriesByUser() throws Exception {
        // Given
        List<InquiryDTO> userInquiries = Arrays.asList(testInquiry);
        Page<InquiryDTO> inquiryPage = new PageImpl<>(userInquiries, PageRequest.of(0, 10), 1);

        when(inquiryService.findByUserNo(eq(1L), any())).thenReturn(inquiryPage);

        // When & Then
        mockMvc.perform(get("/api/inquiries/user/{userNo}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userNo").value(1L));
    }

    @Test
    @DisplayName("카테고리별 문의 조회 테스트")
    void getInquiriesByCategory() throws Exception {
        // Given
        List<InquiryDTO> categoryInquiries = Arrays.asList(testInquiry);
        Page<InquiryDTO> inquiryPage = new PageImpl<>(categoryInquiries, PageRequest.of(0, 10), 1);

        when(inquiryService.findByCategory(eq("GENERAL"), any())).thenReturn(inquiryPage);

        // When & Then
        mockMvc.perform(get("/api/inquiries/category/{category}", "GENERAL")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].category").value("GENERAL"));
    }
}