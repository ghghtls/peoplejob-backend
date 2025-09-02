package com.people.job.payment.service;

import com.people.job.payment.dto.PaymentDTO;

import java.util.List;

public interface PaymentService {

    void processPayment(PaymentDTO dto);  // 결제 처리

    List<PaymentDTO> getPaymentsByUser(Long userNo);  // 내 결제 내역


    void cancelPayment(Long paymentNo);  // 관리자 결제 취소
}
