package com.people.job.scrap.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapDTO {

    private Long scrapNo;

    private Long userNo;
    private Long jobopeningNo;

    private LocalDate regdate;
}
