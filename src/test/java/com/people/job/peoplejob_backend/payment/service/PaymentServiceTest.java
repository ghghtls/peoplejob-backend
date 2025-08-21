package com.people.job.payment.service;

import com.people.job.payment.dto.PaymentDTO;
import com.people.job.payment.entity.PaymentEntity;
import com.people.job.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 서비스 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentEntity testPaymentEntity;
    private PaymentDTO testPaymentDTO;

    @BeforeEach
    void setUp() {
        testPaymentEntity = PaymentEntity.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .status(PaymentEntity.PaymentStatus.COMPLETED)
                .description("프리미엄 서비스 결제")
                .transactionId("TXN123456789")
                .paymentDate(LocalDateTime.now())
                .build();

        testPaymentDTO = PaymentDTO.builder()
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
    @DisplayName("결제 처리 테스트")
    void processPayment() {
        // Given
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(testPaymentEntity);

        // When
        PaymentDTO result = paymentService.processPayment(testPaymentDTO);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(new BigDecimal("10000"), result.getAmount());
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("결제 취소 테스트")
    void cancelPayment() {
        // Given
        PaymentEntity cancelledEntity = PaymentEntity.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .status(PaymentEntity.PaymentStatus.CANCELLED)
                .description("프리미엄 서비스 결제")
                .transactionId("TXN123456789")
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPaymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(cancelledEntity);

        // When
        PaymentDTO result = paymentService.cancelPayment(1L);

        // Then
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("결제 환불 테스트")
    void refundPayment() {
        // Given
        PaymentEntity refundedEntity = PaymentEntity.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .status(PaymentEntity.PaymentStatus.REFUNDED)
                .description("프리미엄 서비스 결제")
                .transactionId("TXN123456789")
                .paymentDate(LocalDateTime.now())
                .refundReason("서비스 불만족")
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPaymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(refundedEntity);

        // When
        PaymentDTO result = paymentService.refundPayment(1L, "서비스 불만족");

        // Then
        assertNotNull(result);
        assertEquals("REFUNDED", result.getStatus());
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("사용자별 결제 내역 조회 테스트")
    void findByUserNo() {
        // Given
        List<PaymentEntity> paymentList = Arrays.asList(testPaymentEntity);
        Page<PaymentEntity> paymentPage = new PageImpl<>(paymentList);
        Pageable pageable = PageRequest.of(0, 10);

        when(paymentRepository.findByUserNo(eq(1L), eq(pageable))).thenReturn(paymentPage);

        // When
        Page<PaymentDTO> result = paymentService.findByUserNo(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getUserNo());
        verify(paymentRepository).findByUserNo(1L, pageable);
    }

    @Test
    @DisplayName("결제 상태별 조회 테스트")
    void findByStatus() {
        // Given
        List<PaymentEntity> paymentList = Arrays.asList(testPaymentEntity);
        Page<PaymentEntity> paymentPage = new PageImpl<>(paymentList);
        Pageable pageable = PageRequest.of(0, 10);

        when(paymentRepository.findByStatus(eq(PaymentEntity.PaymentStatus.COMPLETED), eq(pageable)))
                .thenReturn(paymentPage);

        // When
        Page<PaymentDTO> result = paymentService.findByStatus("COMPLETED", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("COMPLETED", result.getContent().get(0).getStatus());
        verify(paymentRepository).findByStatus(PaymentEntity.PaymentStatus.COMPLETED, pageable);
    }

    @Test
    @DisplayName("결제 ID로 조회 테스트")
    void findById() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPaymentEntity));

        // When
        PaymentDTO result = paymentService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getPaymentNo());
        assertEquals("COMPLETED", result.getStatus());
        verify(paymentRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 결제 조회 시 예외 발생 테스트")
    void findByIdNotFound() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> paymentService.findById(999L));
        verify(paymentRepository).findById(999L);
    }

    @Test
    @DisplayName("결제 금액 검증 테스트")
    void validatePaymentAmount() {
        // Given
        PaymentDTO invalidPayment = PaymentDTO.builder()
                .amount(new BigDecimal("-1000"))
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(invalidPayment));
    }

    @Test
    @DisplayName("이미 취소된 결제 재취소 시도 시 예외 발생 테스트")
    void cancelAlreadyCancelledPayment() {
        // Given
        PaymentEntity cancelledEntity = PaymentEntity.builder()
                .paymentNo(1L)
                .status(PaymentEntity.PaymentStatus.CANCELLED)
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(cancelledEntity));

        // When & Then
        assertThrows(IllegalStateException.class, () -> paymentService.cancelPayment(1L));
        verify(paymentRepository).findById(1L);
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }
}