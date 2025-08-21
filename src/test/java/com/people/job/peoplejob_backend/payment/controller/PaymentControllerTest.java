package com.people.job.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.payment.dto.PaymentDTO;
import com.people.job.payment.service.PaymentService;
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

import java.math.BigDecimal;
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
@DisplayName("결제 컨트롤러 테스트")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentDTO testPayment;

    @BeforeEach
    void setUp() {
        testPayment = PaymentDTO.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .status("COMPLETED")
                .description("프리미엄 서비스 결제")
                .transactionId("TXN123456789")
                .paymentDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("결제 목록 조회 테스트")
    void getPaymentList() throws Exception {
        // Given
        List<PaymentDTO> paymentList = Arrays.asList(testPayment);
        Page<PaymentDTO> paymentPage = new PageImpl<>(paymentList, PageRequest.of(0, 10), 1);

        when(paymentService.findAll(any(), any())).thenReturn(paymentPage);

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("결제 상세 조회 테스트")
    void getPaymentDetail() throws Exception {
        // Given
        when(paymentService.findById(1L)).thenReturn(testPayment);

        // When & Then
        mockMvc.perform(get("/api/payments/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentNo").value(1L))
                .andExpect(jsonPath("$.amount").value(10000))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("결제 요청 테스트")
    void requestPayment() throws Exception {
        // Given
        PaymentDTO paymentRequest = PaymentDTO.builder()
                .userNo(1L)
                .amount(new BigDecimal("20000"))
                .paymentMethod("CARD")
                .description("프리미엄 플러스 서비스 결제")
                .build();

        when(paymentService.processPayment(any(PaymentDTO.class))).thenReturn(testPayment);

        // When & Then
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("결제 취소 테스트")
    void cancelPayment() throws Exception {
        // Given
        PaymentDTO cancelledPayment = PaymentDTO.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .status("CANCELLED")
                .description("프리미엄 서비스 결제")
                .transactionId("TXN123456789")
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentService.cancelPayment(1L)).thenReturn(cancelledPayment);

        // When & Then
        mockMvc.perform(patch("/api/payments/{id}/cancel", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("사용자별 결제 내역 조회 테스트")
    void getPaymentsByUser() throws Exception {
        // Given
        List<PaymentDTO> userPayments = Arrays.asList(testPayment);
        Page<PaymentDTO> paymentPage = new PageImpl<>(userPayments, PageRequest.of(0, 10), 1);

        when(paymentService.findByUserNo(eq(1L), any())).thenReturn(paymentPage);

        // When & Then
        mockMvc.perform(get("/api/payments/user/{userNo}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userNo").value(1L));
    }

    @Test
    @DisplayName("결제 상태별 조회 테스트")
    void getPaymentsByStatus() throws Exception {
        // Given
        List<PaymentDTO> completedPayments = Arrays.asList(testPayment);
        Page<PaymentDTO> paymentPage = new PageImpl<>(completedPayments, PageRequest.of(0, 10), 1);

        when(paymentService.findByStatus(eq("COMPLETED"), any())).thenReturn(paymentPage);

        // When & Then
        mockMvc.perform(get("/api/payments/status/{status}", "COMPLETED")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("결제 환불 테스트")
    void refundPayment() throws Exception {
        // Given
        PaymentDTO refundedPayment = PaymentDTO.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .status("REFUNDED")
                .description("프리미엄 서비스 결제")
                .transactionId("TXN123456789")
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentService.refundPayment(eq(1L), any(String.class))).thenReturn(refundedPayment);

        // When & Then
        mockMvc.perform(patch("/api/payments/{id}/refund", 1L)
                        .param("reason", "서비스 불만족"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    @DisplayName("존재하지 않는 결제 조회 시 404 에러 테스트")
    void getPaymentNotFound() throws Exception {
        // Given
        when(paymentService.findById(999L))
                .thenThrow(new RuntimeException("결제 정보를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/payments/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("잘못된 결제 요청 시 400 에러 테스트")
    void invalidPaymentRequest() throws Exception {
        // Given
        PaymentDTO invalidPayment = PaymentDTO.builder()
                .amount(new BigDecimal("-1000")) // 음수 금액
                .build();

        // When & Then
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayment)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}