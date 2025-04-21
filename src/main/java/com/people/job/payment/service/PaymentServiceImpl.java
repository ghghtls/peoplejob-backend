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
        PaymentEntity entity = PaymentEntity.builder()
                .userNo(dto.getUserNo())
                .jobopeningNo(dto.getJobopeningNo())
                .productName(dto.getProductName())
                .price(dto.getPrice())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status("PAID")
                .build();

        paymentRepository.save(entity);
    }

    @Override
    public List<PaymentDTO> getPaymentsByUser(Long userNo) {
        return paymentRepository.findByUserNo(userNo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByJobopening(Long jobopeningNo) {
        return paymentRepository.findByJobopeningNo(jobopeningNo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelPayment(Long paymentNo) {
        PaymentEntity entity = paymentRepository.findById(paymentNo)
                .orElseThrow(() -> new RuntimeException("결제 내역이 없습니다."));

        entity.setStatus("CANCELLED");
        paymentRepository.save(entity);
    }

    private PaymentDTO toDTO(PaymentEntity e) {
        return PaymentDTO.builder()
                .paymentNo(e.getPaymentNo())
                .userNo(e.getUserNo())
                .jobopeningNo(e.getJobopeningNo())
                .productName(e.getProductName())
                .price(e.getPrice())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .status(e.getStatus())
                .build();
    }
}
