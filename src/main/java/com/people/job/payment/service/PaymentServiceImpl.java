package com.people.job.payment.service;

import com.people.job.payment.dto.PaymentDTO;
import com.people.job.payment.entity.PaymentEntity;
import com.people.job.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public void processPayment(PaymentDTO dto) {
        // 스키마에 존재하는 필드만 매핑
        PaymentEntity entity = PaymentEntity.builder()
                .userNo(dto.getUserNo())
                .amount(dto.getAmount())                     // price → amount
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus("PAID")                       // status → paymentStatus
                // paymentDate는 DB DEFAULT CURRENT_TIMESTAMP 사용
                .description(dto.getDescription())           // 설명(선택)
                .build();

        paymentRepository.save(entity);
    }

    @Override
    public List<PaymentDTO> getPaymentsByUser(Long userNo) {
        return paymentRepository.findByUserNo(userNo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ❌ 스키마에 jobopeningNo 컬럼이 없으므로 제거가 맞음.
    // 기존 시그니처를 꼭 유지해야 한다면, UnsupportedOperationException 처리 권장.
    @Override
    public List<PaymentDTO> getPaymentsByJobopening(Long jobopeningNo) {
        throw new UnsupportedOperationException("payment 테이블에 jobopeningNo 컬럼이 없어 조회할 수 없습니다.");
    }

    @Override
    public void cancelPayment(Long paymentNo) {
        PaymentEntity entity = paymentRepository.findById(paymentNo)
                .orElseThrow(() -> new RuntimeException("결제 내역이 없습니다."));

        entity.setPaymentStatus("CANCELLED");               // status → paymentStatus
        paymentRepository.save(entity);
    }

    private PaymentDTO toDTO(PaymentEntity e) {
        return PaymentDTO.builder()
                .paymentNo(e.getPaymentNo())
                .userNo(e.getUserNo())
                .amount(e.getAmount())
                .paymentMethod(e.getPaymentMethod())
                .paymentStatus(e.getPaymentStatus())
                .paymentDate(e.getPaymentDate())
                .description(e.getDescription())
                .build();
    }
}
