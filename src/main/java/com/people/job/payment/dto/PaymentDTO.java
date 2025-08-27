package com.people.job.payment.dto;

import com.people.job.payment.entity.PaymentEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    private Long paymentNo;
    private Long userNo;
    private BigDecimal amount; // DB 스키마와 맞춤 (int price -> BigDecimal amount)
    private String paymentMethod; // DB 스키마와 맞춤 (추가)
    private String paymentStatus; // DB 스키마와 맞춤 (status -> paymentStatus)
    private LocalDateTime paymentDate; // DB 스키마와 맞춤 (TIMESTAMP)
    private String description; // DB 스키마와 맞춤 (추가)

    // Entity -> DTO 변환
    public static PaymentDTO fromEntity(PaymentEntity entity) {
        return PaymentDTO.builder()
                .paymentNo(entity.getPaymentNo())
                .userNo(entity.getUserNo())
                .amount(entity.getAmount())
                .paymentMethod(entity.getPaymentMethod())
                .paymentStatus(entity.getPaymentStatus())
                .paymentDate(entity.getPaymentDate())
                .description(entity.getDescription())
                .build();
    }

    // DTO -> Entity 변환
    public PaymentEntity toEntity() {
        return PaymentEntity.builder()
                .paymentNo(this.paymentNo)
                .userNo(this.userNo)
                .amount(this.amount)
                .paymentMethod(this.paymentMethod)
                .paymentStatus(this.paymentStatus != null ? this.paymentStatus : "PENDING")
                .description(this.description)
                .build();
    }

    // 검증 메서드
    public boolean isValid() {
        return userNo != null &&
                amount != null &&
                amount.compareTo(BigDecimal.ZERO) > 0 &&
                paymentMethod != null && !paymentMethod.trim().isEmpty();
    }

    // 결제 상태 확인 메서드들
    public boolean isPending() {
        return "PENDING".equals(this.paymentStatus);
    }

    public boolean isSuccess() {
        return "SUCCESS".equals(this.paymentStatus);
    }

    public boolean isFailed() {
        return "FAILED".equals(this.paymentStatus);
    }

    public boolean isCanceled() {
        return "CANCELED".equals(this.paymentStatus);
    }

    // 상태 설명 반환
    public String getStatusDescription() {
        if (paymentStatus == null) return "알 수 없음";

        switch (paymentStatus) {
            case "PENDING": return "결제 대기";
            case "SUCCESS": return "결제 완료";
            case "FAILED": return "결제 실패";
            case "CANCELED": return "결제 취소";
            default: return paymentStatus;
        }
    }

    // 금액 포맷팅 (예: 10000 -> "10,000원")
    public String getFormattedAmount() {
        if (amount == null) return "0원";
        return String.format("%,.0f원", amount);
    }
}