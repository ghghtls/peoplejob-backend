package com.people.job.resume.dto;

import com.people.job.resume.entity.ResumeEntity;
import lombok.*;

import java.time.LocalDate;

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
    private LocalDate regdate; // DB 스키마와 맞춤 (String -> LocalDate)
    private String imagePath;
    private String originalImage;
    private Long userNo;

    // Entity -> DTO 변환
    public static ResumeDTO fromEntity(ResumeEntity entity) {
        return ResumeDTO.builder()
                .resumeNo(entity.getResumeNo())
                .title(entity.getTitle())
                .content(entity.getContent())
                .education(entity.getEducation())
                .career(entity.getCareer())
                .certificate(entity.getCertificate())
                .hopeJobtype(entity.getHopeJobtype())
                .hopeLocation(entity.getHopeLocation())
                .salary(entity.getSalary())
                .workType(entity.getWorkType())
                .regdate(entity.getRegdate())
                .imagePath(entity.getImagePath())
                .originalImage(entity.getOriginalImage())
                .userNo(entity.getUserNo())
                .build();
    }

    // DTO -> Entity 변환
    public ResumeEntity toEntity() {
        return ResumeEntity.builder()
                .resumeNo(this.resumeNo)
                .title(this.title)
                .content(this.content)
                .education(this.education)
                .career(this.career)
                .certificate(this.certificate)
                .hopeJobtype(this.hopeJobtype)
                .hopeLocation(this.hopeLocation)
                .salary(this.salary)
                .workType(this.workType)
                .imagePath(this.imagePath)
                .originalImage(this.originalImage)
                .userNo(this.userNo)
                .build();
    }

    // 검증 메서드들
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
                content != null && !content.trim().isEmpty() &&
                education != null && !education.trim().isEmpty() &&
                career != null && !career.trim().isEmpty() &&
                hopeJobtype != null && !hopeJobtype.trim().isEmpty() &&
                hopeLocation != null && !hopeLocation.trim().isEmpty() &&
                workType != null && !workType.trim().isEmpty() &&
                userNo != null;
    }

    public boolean hasImage() {
        return imagePath != null && !imagePath.trim().isEmpty();
    }
}