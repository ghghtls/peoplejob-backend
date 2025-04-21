package com.people.job.apply.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyDTO {

    private Long applyNo;

    private Long resumeNo;
    private Long jobopeningNo;

    private LocalDate regdate;
}
