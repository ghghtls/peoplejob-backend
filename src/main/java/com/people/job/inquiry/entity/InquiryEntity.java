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

    private Long userNo;

    private String title;
    private String content;

    private LocalDate regdate;

    private String answer;
    private LocalDate answerDate;

    private String status; // WAIT, ANSWERED
}
