package com.people.job.payment.controller;

import com.people.job.payment.dto.PaymentDTO;
import com.people.job.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> pay(@RequestBody PaymentDTO dto) {
        paymentService.processPayment(dto);
        return ResponseEntity.ok("결제 완료!");
    }

    @GetMapping("/user/{userNo}")
    public ResponseEntity<List<PaymentDTO>> userPayments(@PathVariable Long userNo) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userNo));
    }



    @PutMapping("/cancel/{paymentNo}")
    public ResponseEntity<?> cancel(@PathVariable Long paymentNo) {
        paymentService.cancelPayment(paymentNo);
        return ResponseEntity.ok("결제 취소 처리됨");
    }
}
