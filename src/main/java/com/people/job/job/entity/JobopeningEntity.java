package com.people.job.job.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "jobopening")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobopeningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobopeningNo;

    @Column(nullable = false)
    private String title;

    private String content;
    private String jobtype;
    private String location;
    private String education;
    private String career;
    private String salary;

    private LocalDate regdate;
    private LocalDate deadline;

    private Long companyNo; // 기업회원 ID 외래키

    private String filename;
    private String originalFilename;
}
