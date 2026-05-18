package com.people.job.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @Column(name = "payment_no")
    private Long paymentNo;

    @Column(nullable = false)
    private Long userNo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // DB 스키마의 DECIMAL(10,2)와 맞춤

    @Column(length = 50, nullable = false)
    private String paymentMethod;

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String paymentStatus = "PENDING"; // PENDING, SUCCESS, FAILED, CANCELED

    @Column(nullable = false)
    private LocalDateTime paymentDate; // DB 스키마의 TIMESTAMP와 맞춤

    @Column(columnDefinition = "TEXT")
    private String description;

    private Long jobNo;

    private java.time.LocalDateTime adEndDate;

    @PrePersist
    protected void onCreate() {
        this.paymentDate = LocalDateTime.now();
        if (this.paymentStatus == null) {
            this.paymentStatus = "PENDING";
        }
    }
}