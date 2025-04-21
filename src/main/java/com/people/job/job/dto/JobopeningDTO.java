package com.people.job.job.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobopeningDTO {

    private Long jobopeningNo;

    private String title;
    private String content;
    private String jobtype;
    private String location;
    private String education;
    private String career;
    private String salary;

    private LocalDate regdate;
    private LocalDate deadline;

    private Long companyNo;

    private String filename;
    private String originalFilename;
}
