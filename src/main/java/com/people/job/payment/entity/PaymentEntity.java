package com.people.job.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentNo;

    private Long userNo;         // 기업회원 번호
    private Long jobopeningNo;   // 광고할 공고 번호

    private String productName;  // 광고 상품명
    private int price;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;       // "PAID", "CANCELLED"
}
