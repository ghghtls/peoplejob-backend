package com.people.job.payment.service;

import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
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
    private final JobopeningRepository jobopeningRepository;

    @Override
    public void processPayment(PaymentDTO dto) {
        PaymentEntity entity = PaymentEntity.builder()
                .userNo(dto.getUserNo())
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus("PAID")
                .description(dto.getDescription())
                .jobNo(dto.getJobNo())
                .adEndDate(dto.getAdEndDate())
                .build();

        paymentRepository.save(entity);

        // 채용공고에 광고 플래그 설정
        if (dto.getJobNo() != null) {
            jobopeningRepository.findById(dto.getJobNo()).ifPresent(job -> {
                job.setIsAdvertised(true);
                job.setAdEndDate(dto.getAdEndDate());
                jobopeningRepository.save(job);
            });
        }
    }

    @Override
    public List<PaymentDTO> getPaymentsByUser(Long userNo) {
        return paymentRepository.findByUserNo(userNo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
                .jobNo(e.getJobNo())
                .adEndDate(e.getAdEndDate())
                .build();
    }
}
