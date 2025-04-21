package com.people.job.resume.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeDTO {

    private Long resumeNo;

    private String title;
    private String content;
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

    private Long userNo;
}
