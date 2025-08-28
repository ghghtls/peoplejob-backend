package com.people.job.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false, length = 255)
    private String email;

    // 스키마 컬럼명: user_id
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    // 스키마 컬럼명: expiry_date
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean used = false;

    // 스키마 컬럼명: created_at
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 만료 여부 */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    /** 사용 가능 여부 */
    public boolean isValid() {
        return Boolean.FALSE.equals(used) && !isExpired();
    }

    /** 사용 처리 */
    public void markAsUsed() {
        this.used = true;
    }

    /** 기본값은 "값이 비어있을 때만" 채우기 */
    @PrePersist
    protected void onCreate() {
        if (used == null) used = false;
        if (expiryDate == null) {
            // 서비스에서 따로 TTL을 지정하지 않았다면 15분 기본값
            expiryDate = LocalDateTime.now().plusMinutes(15);
        }
        // createdAt은 @CreationTimestamp가 채워줌
    }
}
