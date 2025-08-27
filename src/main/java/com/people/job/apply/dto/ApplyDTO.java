package com.people.job.apply.dto;

import com.people.job.apply.entity.ApplyEntity;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyDTO {

    private Long applyNo;
    private Long jobNo; // DB 스키마와 맞춤 (jobopeningNo -> jobNo)
    private Long userNo; // 지원자 ID 추가
    private Long resumeNo;
    private LocalDate applyDate; // DB 스키마와 맞춤 (regdate -> applyDate)
    private String status; // 지원 상태 추가
    private String message; // 지원 메시지 추가

    // 추가 정보 (조인해서 가져올 수 있는 정보들)
    private String jobTitle; // 채용공고 제목
    private String companyName; // 회사명
    private String applicantName; // 지원자명
    private String resumeTitle; // 이력서 제목

    // Entity -> DTO 변환
    public static ApplyDTO fromEntity(ApplyEntity entity) {
        return ApplyDTO.builder()
                .applyNo(entity.getApplyNo())
                .jobNo(entity.getJobNo())
                .userNo(entity.getUserNo())
                .resumeNo(entity.getResumeNo())
                .applyDate(entity.getApplyDate())
                .status(entity.getStatus())
                .message(entity.getMessage())
                .build();
    }

    // DTO -> Entity 변환
    public ApplyEntity toEntity() {
        return ApplyEntity.builder()
                .applyNo(this.applyNo)
                .jobNo(this.jobNo)
                .userNo(this.userNo)
                .resumeNo(this.resumeNo)
                .status(this.status != null ? this.status : "PENDING")
                .message(this.message)
                .build();
    }

    // 검증 메서드
    public boolean isValid() {
        return jobNo != null && userNo != null && resumeNo != null;
    }

    // 상태 확인 메서드들
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    public boolean isAccepted() {
        return "ACCEPTED".equals(this.status);
    }

    public boolean isRejected() {
        return "REJECTED".equals(this.status);
    }

    public boolean isCanceled() {
        return "CANCELED".equals(this.status);
    }

    public String getStatusDescription() {
        if (status == null) return "알 수 없음";

        switch (status) {
            case "PENDING": return "지원완료";
            case "REVIEWED": return "검토중";
            case "ACCEPTED": return "합격";
            case "REJECTED": return "불합격";
            case "CANCELED": return "지원취소";
            default: return status;
        }
    }
}