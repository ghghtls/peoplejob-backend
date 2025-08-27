package com.people.job.resume.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resumeNo;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 자기소개서

    @Column(length = 500, nullable = false)
    private String education;

    @Column(length = 500, nullable = false)
    private String career;

    @Column(length = 500)
    private String certificate;

    @Column(length = 50, nullable = false)
    private String hopeJobtype;

    @Column(length = 50, nullable = false)
    private String hopeLocation;

    @Column(length = 50)
    private String salary;

    @Column(length = 50, nullable = false)
    private String workType;

    @Column(nullable = false)
    private LocalDate regdate; // DB 스키마의 DATE 타입과 맞춤

    @Column(length = 200)
    private String imagePath;

    @Column(length = 200)
    private String originalImage;

    @Column(nullable = false)
    private Long userNo; // 외래키, NOT NULL

    @PrePersist
    protected void onCreate() {
        this.regdate = LocalDate.now();
    }
}