package com.people.job.peoplejob_backend.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.payment.dto.PaymentDTO;
import com.people.job.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("결제 컨트롤러 테스트")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private PaymentDTO testPayment;

    @BeforeEach
    void setUp() {
        testPayment = PaymentDTO.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(new BigDecimal("10000")) // 실제 DTO 필드명
                .paymentMethod("CARD") // 실제 DTO 필드명
                .paymentStatus("SUCCESS") // status -> paymentStatus로 수정
                .description("프리미엄 서비스 결제")
                .paymentDate(LocalDateTime.now()) // 실제 DTO 필드명
                .build();
    }

    @Test
    @DisplayName("결제 요청 테스트")
    void processPayment() throws Exception {
        // Given
        PaymentDTO paymentRequest = PaymentDTO.builder()
                .userNo(1L)
                .amount(new BigDecimal("20000"))
                .paymentMethod("CARD")
                .description("프리미엄 플러스 서비스 결제")
                .build();

        // When & Then
        mockMvc.perform(post("/api/payment") // 실제 경로: /api/payment
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andDo(print())
                .andExpect(status().isOk()) // 실제 응답: 200 OK
                .andExpect(content().string("결제 완료!")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("사용자별 결제 내역 조회 테스트")
    void getUserPayments() throws Exception {
        // Given
        List<PaymentDTO> userPayments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByUser(1L)).thenReturn(userPayments);

        // When & Then
        mockMvc.perform(get("/api/payment/user/{userNo}", 1L)) // 실제 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userNo").value(1L))
                .andExpect(jsonPath("$[0].paymentStatus").value("SUCCESS")); // 실제 필드명
    }

    @Test
    @DisplayName("채용공고별 결제 내역 조회 테스트")
    void getJobPayments() throws Exception {
        // Given
        List<PaymentDTO> jobPayments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByJobopening(1L)).thenReturn(jobPayments); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/payment/job/{jobopeningNo}", 1L)) // 실제 경로와 매개변수명
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].paymentStatus").value("SUCCESS"));
    }

    @Test
    @DisplayName("결제 취소 테스트")
    void cancelPayment() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/payment/cancel/{paymentNo}", 1L)) // 실제 경로와 HTTP 메서드
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("결제 취소 처리됨")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("잘못된 결제 요청 시 오류 테스트")
    void invalidPaymentRequest() throws Exception {
        // Given - 실제 ServiceImpl에서는 필드 검증이 없을 수 있으므로 다른 방식으로 테스트
        PaymentDTO invalidPayment = PaymentDTO.builder()
                .userNo(null) // 필수 값 누락
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .build();

        // 실제 ServiceImpl에서 예외가 발생하도록 Mock 설정
        doThrow(new RuntimeException("사용자 정보가 필요합니다."))
                .when(paymentService).processPayment(any(PaymentDTO.class));

        // When & Then
        mockMvc.perform(post("/api/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayment)))
                .andDo(print())
                .andExpect(status().isBadRequest()); // Controller에서 예외 처리 시
    }

    @Test
    @DisplayName("존재하지 않는 결제 취소 시 오류 테스트")
    void cancelNonExistentPayment() throws Exception {
        // Given
        doThrow(new RuntimeException("결제 내역이 없습니다."))
                .when(paymentService).cancelPayment(999L);

        // When & Then
        mockMvc.perform(put("/api/payment/cancel/{paymentNo}", 999L))
                .andDo(print())
                .andExpect(status().isBadRequest()); // Controller에서 예외 처리 시
    }

    @Test
    @DisplayName("빈 결제 목록 조회 테스트")
    void getEmptyPaymentList() throws Exception {
        // Given
        when(paymentService.getPaymentsByUser(999L)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/payment/user/{userNo}", 999L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("여러 결제 내역 조회 테스트")
    void getMultiplePayments() throws Exception {
        // Given
        PaymentDTO payment2 = PaymentDTO.builder()
                .paymentNo(2L)
                .userNo(1L)
                .amount(new BigDecimal("5000"))
                .paymentMethod("BANK")
                .paymentStatus("SUCCESS")
                .description("기본 서비스 결제")
                .paymentDate(LocalDateTime.now().minusDays(1))
                .build();

        List<PaymentDTO> multiplePayments = Arrays.asList(testPayment, payment2);
        when(paymentService.getPaymentsByUser(1L)).thenReturn(multiplePayments);

        // When & Then
        mockMvc.perform(get("/api/payment/user/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].amount").value(10000))
                .andExpect(jsonPath("$[1].amount").value(5000));
    }

    @Test
    @DisplayName("결제 방법별 테스트")
    void testDifferentPaymentMethods() throws Exception {
        // Given - 각기 다른 결제 방법으로 결제 요청
        PaymentDTO cardPayment = PaymentDTO.builder()
                .userNo(1L)
                .amount(new BigDecimal("15000"))
                .paymentMethod("CARD")
                .description("신용카드 결제")
                .build();

        PaymentDTO bankPayment = PaymentDTO.builder()
                .userNo(1L)
                .amount(new BigDecimal("12000"))
                .paymentMethod("BANK")
                .description("계좌이체 결제")
                .build();

        // When & Then - 신용카드 결제
        mockMvc.perform(post("/api/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardPayment)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("결제 완료!"));

        // When & Then - 계좌이체 결제
        mockMvc.perform(post("/api/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bankPayment)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("결제 완료!"));
    }
}