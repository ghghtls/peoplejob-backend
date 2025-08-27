package com.people.job.apply.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "apply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applyNo;

    @Column(nullable = false)
    private Long jobNo; // DB 스키마에 맞춤 (jobopeningNo -> jobNo)

    @Column(nullable = false)
    private Long userNo; // 지원자 ID

    @Column(nullable = false)
    private Long resumeNo; // 사용한 이력서 ID

    @Column(nullable = false)
    private LocalDate applyDate; // DB 스키마의 applyDate와 맞춤

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, REVIEWED, ACCEPTED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String message; // 지원 메시지

    @PrePersist
    protected void onCreate() {
        this.applyDate = LocalDate.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }
}