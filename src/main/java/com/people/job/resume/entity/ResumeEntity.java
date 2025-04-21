package com.people.job.resume.entity;

import jakarta.persistence.*;
import lombok.*;

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

    private String title;
    private String content; // 자기소개서
    private String education;
    private String career;
    private String certificate;
    private String hopeJobtype;
    private String hopeLocation;
    private String salary;
    private String workType;

    private String regdate;

    private String imagePath;
    private String originalImage;

    private Long userNo; // 개인회원 번호 (FK)
}
