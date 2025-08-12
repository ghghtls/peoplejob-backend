package com.people.job.notice.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDTO {

    private Long noticeNo;
    private String title;
    private String content;
    private String writer;
    private LocalDate regdate;
    private Integer viewCount;
    private Boolean isImportant;
    private Boolean isActive;
    private String filename;
    private String originalFilename;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 요약 내용 반환 (목록용)
    public String getContentSummary() {
        if (content == null || content.length() <= 100) {
            return content;
        }
        return content.substring(0, 100) + "...";
    }

    // 중요 공지 여부 확인
    public boolean isImportantNotice() {
        return isImportant != null && isImportant;
    }

    // 활성 공지 여부 확인
    public boolean isActiveNotice() {
        return isActive != null && isActive;
    }

    // 첨부파일 존재 여부
    public boolean hasAttachment() {
        return filename != null && !filename.trim().isEmpty();
    }
}