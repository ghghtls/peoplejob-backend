package com.people.job.inquiry.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryDTO {

    private Long inquiryNo;
    private Long userNo;

    private String title;
    private String content;
    private LocalDate regdate;

    private String answer;
    private LocalDate answerDate;

    private String status;
}
