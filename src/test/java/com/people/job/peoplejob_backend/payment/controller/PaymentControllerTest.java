package com.people.job.peoplejob_backend.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.payment.controller.PaymentController;
import com.people.job.payment.dto.PaymentDTO;
import com.people.job.payment.service.PaymentService;
import com.people.job.user.security.JwtTokenProvider;
import com.people.job.user.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("결제 컨트롤러 테스트")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private PaymentDTO testPayment;

    @BeforeEach
    void setUp() {
        testPayment = PaymentDTO.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .paymentStatus("SUCCESS")
                .description("프리미엄 서비스 결제")
                .paymentDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("결제 요청 테스트")
    void processPayment() throws Exception {
        PaymentDTO paymentRequest = PaymentDTO.builder()
                .userNo(1L)
                .amount(new BigDecimal("20000"))
                .paymentMethod("CARD")
                .description("프리미엄 플러스 서비스 결제")
                .build();

        mockMvc.perform(post("/api/payment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("결제 완료!"));
    }

    @Test
    @DisplayName("사용자별 결제 내역 조회 테스트")
    void getUserPayments() throws Exception {
        List<PaymentDTO> userPayments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByUser(1L)).thenReturn(userPayments);

        mockMvc.perform(get("/api/payment/user/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userNo").value(1L))
                .andExpect(jsonPath("$[0].paymentStatus").value("SUCCESS"));
    }

    @Test
    @DisplayName("결제 취소 테스트")
    void cancelPayment() throws Exception {
        mockMvc.perform(put("/api/payment/cancel/{paymentNo}", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("결제 취소 처리됨"));
    }

    @Test
    @DisplayName("잘못된 결제 요청 시 오류 테스트")
    void invalidPaymentRequest() throws Exception {
        PaymentDTO invalidPayment = PaymentDTO.builder()
                .userNo(null)
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .build();
        doThrow(new RuntimeException("사용자 정보가 필요합니다."))
                .when(paymentService).processPayment(any(PaymentDTO.class));

        mockMvc.perform(post("/api/payment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayment)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 결제 취소 시 오류 테스트")
    void cancelNonExistentPayment() throws Exception {
        doThrow(new RuntimeException("결제 내역이 없습니다."))
                .when(paymentService).cancelPayment(999L);

        mockMvc.perform(put("/api/payment/cancel/{paymentNo}", 999L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("빈 결제 목록 조회 테스트")
    void getEmptyPaymentList() throws Exception {
        when(paymentService.getPaymentsByUser(999L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/payment/user/{userNo}", 999L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("여러 결제 내역 조회 테스트")
    void getMultiplePayments() throws Exception {
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

        mockMvc.perform(post("/api/payment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardPayment)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("결제 완료!"));

        mockMvc.perform(post("/api/payment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bankPayment)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("결제 완료!"));
    }
}
