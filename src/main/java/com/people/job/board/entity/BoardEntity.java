package com.people.job.board.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "board")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_no")
    private Long boardNo;

    private String category;  // 예: 자료실, 공지사항, 뉴스

    private String title;
    private String content;

    private String writer;
    private LocalDate regdate;

    private String filename;
    @Column(name = "originalFilename")
    private String originalFilename;

    private int viewCount;
    private Boolean isActive;       // default true
    private Boolean allowUpload;    // default false
    private Boolean allowComment;   // default true
}
