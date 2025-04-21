package com.people.job.payment.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    private Long paymentNo;

    private Long userNo;
    private Long jobopeningNo;

    private String productName;
    private int price;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;
}
