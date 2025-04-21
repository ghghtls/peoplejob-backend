package com.people.job.board.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDTO {

    private Long boardNo;

    private String category;
    private String title;
    private String content;
    private String writer;
    private LocalDate regdate;

    private String filename;
    private String originalFilename;

    private int viewCount;
}
