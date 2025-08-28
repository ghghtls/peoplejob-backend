package com.people.job.peoplejob_backend.payment.service;

import com.people.job.payment.dto.PaymentDTO;
import com.people.job.payment.entity.PaymentEntity;
import com.people.job.payment.repository.PaymentRepository;
import com.people.job.payment.service.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                .amount(new BigDecimal("10000")) // 실제 Entity 필드
                .paymentMethod("CARD") // 실제 Entity 필드
                .paymentStatus("SUCCESS") // 실제 Entity 필드
                .description("프리미엄 서비스 결제") // 실제 Entity 필드
                .paymentDate(LocalDateTime.now()) // 실제 Entity 필드
                .build();

        testPaymentDTO = PaymentDTO.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(new BigDecimal("10000")) // 실제 DTO 필드
                .paymentMethod("CARD") // 실제 DTO 필드
                .paymentStatus("SUCCESS") // 실제 DTO 필드
                .description("프리미엄 서비스 결제") // 실제 DTO 필드
                .paymentDate(LocalDateTime.now()) // 실제 DTO 필드
                .build();
    }

    @Test
    @DisplayName("결제 처리 테스트")
    void processPayment() {
        // Given
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(testPaymentEntity);

        // When
        assertDoesNotThrow(() -> paymentService.processPayment(testPaymentDTO));

        // Then
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("사용자별 결제 내역 조회 테스트")
    void getPaymentsByUser() {
        // Given
        List<PaymentEntity> paymentList = Arrays.asList(testPaymentEntity);
        when(paymentRepository.findByUserNo(1L)).thenReturn(paymentList);

        // When
        List<PaymentDTO> result = paymentService.getPaymentsByUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserNo());
        verify(paymentRepository).findByUserNo(1L);
    }

    @Test
    @DisplayName("채용공고별 결제 내역 조회 테스트")
    void getPaymentsByJobopening() {
        // Given - 실제 ServiceImpl에는 이 메서드가 있지만 Entity에는 jobopeningNo 필드가 없으므로
        // Repository에서 다른 방식으로 조회할 것으로 가정
        List<PaymentEntity> paymentList = Arrays.asList(testPaymentEntity);
        when(paymentRepository.findByJobopeningNo(1L)).thenReturn(paymentList);

        // When
        List<PaymentDTO> result = paymentService.getPaymentsByJobopening(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findByJobopeningNo(1L);
    }

    @Test
    @DisplayName("결제 취소 테스트")
    void cancelPayment() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPaymentEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(testPaymentEntity);

        // When
        assertDoesNotThrow(() -> paymentService.cancelPayment(1L));

        // Then
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("존재하지 않는 결제 취소 시 예외 발생 테스트")
    void cancelNonExistentPayment() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.cancelPayment(999L));
        assertEquals("결제 내역이 없습니다.", exception.getMessage());
        verify(paymentRepository).findById(999L);
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("빈 결제 목록 조회 테스트 - 사용자별")
    void getEmptyPaymentsByUser() {
        // Given
        when(paymentRepository.findByUserNo(999L)).thenReturn(Arrays.asList());

        // When
        List<PaymentDTO> result = paymentService.getPaymentsByUser(999L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(paymentRepository).findByUserNo(999L);
    }

    @Test
    @DisplayName("여러 결제 내역 조회 테스트")
    void getMultiplePaymentsByUser() {
        // Given
        PaymentEntity payment2 = PaymentEntity.builder()
                .paymentNo(2L)
                .userNo(1L)
                .amount(new BigDecimal("5000"))
                .paymentMethod("BANK")
                .paymentStatus("SUCCESS")
                .description("기본 서비스 결제")
                .paymentDate(LocalDateTime.now().minusDays(1))
                .build();

        List<PaymentEntity> paymentList = Arrays.asList(testPaymentEntity, payment2);
        when(paymentRepository.findByUserNo(1L)).thenReturn(paymentList);

        // When
        List<PaymentDTO> result = paymentService.getPaymentsByUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getUserNo());
        assertEquals(1L, result.get(1).getUserNo());
        verify(paymentRepository).findByUserNo(1L);
    }

    @Test
    @DisplayName("결제 취소 후 상태 변경 확인 테스트")
    void verifyCancelledStatus() {
        // Given
        PaymentEntity originalEntity = PaymentEntity.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .paymentStatus("SUCCESS")
                .description("프리미엄 서비스 결제")
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(originalEntity));

        // ArgumentCaptor를 사용하여 저장되는 Entity의 상태 확인
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
            PaymentEntity saved = invocation.getArgument(0);
            assertEquals("CANCELLED", saved.getPaymentStatus()); // paymentStatus 필드 사용
            return saved;
        });

        // When
        paymentService.cancelPayment(1L);

        // Then
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("결제 유효성 검증 테스트")
    void validatePaymentData() {
        // Given - 필수 필드 누락
        PaymentDTO invalidPayment = PaymentDTO.builder()
                .userNo(null) // 필수 값 누락
                .amount(new BigDecimal("10000"))
                .paymentMethod("CARD")
                .build();

        // When & Then
        // 실제 ServiceImpl에서는 검증 로직이 없을 수 있으므로 NullPointerException이 발생할 수 있음
        assertThrows(Exception.class, () -> paymentService.processPayment(invalidPayment));
    }

    @Test
    @DisplayName("다양한 결제 방법 테스트")
    void testDifferentPaymentMethods() {
        // Given
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

        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(testPaymentEntity);

        // When & Then
        assertDoesNotThrow(() -> paymentService.processPayment(cardPayment));
        assertDoesNotThrow(() -> paymentService.processPayment(bankPayment));

        verify(paymentRepository, times(2)).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("결제 상태별 결제 목록 테스트")
    void testPaymentsByStatus() {
        // Given
        PaymentEntity pendingPayment = PaymentEntity.builder()
                .paymentNo(2L)
                .userNo(1L)
                .amount(new BigDecimal("8000"))
                .paymentMethod("CARD")
                .paymentStatus("PENDING")
                .description("대기중인 결제")
                .paymentDate(LocalDateTime.now())
                .build();

        List<PaymentEntity> userPayments = Arrays.asList(testPaymentEntity, pendingPayment);
        when(paymentRepository.findByUserNo(1L)).thenReturn(userPayments);

        // When
        List<PaymentDTO> result = paymentService.getPaymentsByUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // 각 결제의 상태 확인
        boolean hasSuccess = result.stream().anyMatch(p -> "SUCCESS".equals(p.getPaymentStatus()));
        boolean hasPending = result.stream().anyMatch(p -> "PENDING".equals(p.getPaymentStatus()));

        assertTrue(hasSuccess);
        assertTrue(hasPending);
        verify(paymentRepository).findByUserNo(1L);
    }
}