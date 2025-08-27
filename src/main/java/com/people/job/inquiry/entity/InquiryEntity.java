package com.people.job.inquiry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "inquiry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryNo;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 50, nullable = false)
    private String writer;

    @Column(length = 100, nullable = false)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDate regdate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isAnswered = false;

    @Column(columnDefinition = "TEXT")
    private String answer;

    private LocalDate answerDate;

    @Column(length = 50)
    private String answerBy;

    @PrePersist
    protected void onCreate() {
        this.regdate = LocalDate.now();
        if (this.isAnswered == null) {
            this.isAnswered = false;
        }
    }
}