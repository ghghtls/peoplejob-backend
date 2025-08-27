package com.people.job.inquiry.dto;

import com.people.job.inquiry.entity.InquiryEntity;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryDTO {

    private Long inquiryNo;
    private String title;
    private String content;
    private String writer; // DB 스키마에 맞춤 (추가)
    private String email; // DB 스키마에 맞춤 (추가)
    private String phone; // DB 스키마에 맞춤 (추가)
    private String category; // DB 스키마에 맞춤 (추가)
    private LocalDate regdate;
    private Boolean isAnswered; // DB 스키마에 맞춤 (추가)
    private String answer;
    private LocalDate answerDate;
    private String answerBy; // DB 스키마에 맞춤 (추가)

    // Entity -> DTO 변환
    public static InquiryDTO fromEntity(InquiryEntity entity) {
        return InquiryDTO.builder()
                .inquiryNo(entity.getInquiryNo())
                .title(entity.getTitle())
                .content(entity.getContent())
                .writer(entity.getWriter())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .category(entity.getCategory())
                .regdate(entity.getRegdate())
                .isAnswered(entity.getIsAnswered())
                .answer(entity.getAnswer())
                .answerDate(entity.getAnswerDate())
                .answerBy(entity.getAnswerBy())
                .build();
    }

    // DTO -> Entity 변환
    public InquiryEntity toEntity() {
        return InquiryEntity.builder()
                .inquiryNo(this.inquiryNo)
                .title(this.title)
                .content(this.content)
                .writer(this.writer)
                .email(this.email)
                .phone(this.phone)
                .category(this.category)
                .isAnswered(this.isAnswered != null ? this.isAnswered : false)
                .answer(this.answer)
                .answerDate(this.answerDate)
                .answerBy(this.answerBy)
                .build();
    }

    // 검증 메서드
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
                content != null && !content.trim().isEmpty() &&
                writer != null && !writer.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                category != null && !category.trim().isEmpty();
    }

    // 이메일 검증
    public boolean isValidEmail() {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // 답변 여부 확인
    public boolean isAnswered() {
        return Boolean.TRUE.equals(this.isAnswered);
    }

    // 상태 텍스트 반환
    public String getStatusText() {
        return isAnswered() ? "답변완료" : "답변대기";
    }
}