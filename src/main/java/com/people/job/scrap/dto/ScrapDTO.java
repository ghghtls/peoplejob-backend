package com.people.job.scrap.dto;

import com.people.job.scrap.entity.ScrapEntity;
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
    private Long jobNo; // DB 스키마와 맞춤 (jobopeningNo -> jobNo)
    private LocalDate scrapDate; // DB 스키마와 맞춤 (regdate -> scrapDate)

    // 추가 정보 (조인해서 가져올 수 있는 정보들)
    private String jobTitle; // 채용공고 제목
    private String companyName; // 회사명
    private String location; // 지역
    private String jobType; // 직종
    private LocalDate deadline; // 마감일
    private Boolean isExpired; // 마감 여부

    // Entity -> DTO 변환
    public static ScrapDTO fromEntity(ScrapEntity entity) {
        return ScrapDTO.builder()
                .scrapNo(entity.getScrapNo())
                .userNo(entity.getUserNo())
                .jobNo(entity.getJobNo())
                .scrapDate(entity.getScrapDate())
                .build();
    }

    // DTO -> Entity 변환
    public ScrapEntity toEntity() {
        return ScrapEntity.builder()
                .scrapNo(this.scrapNo)
                .userNo(this.userNo)
                .jobNo(this.jobNo)
                .build();
    }

    // 검증 메서드
    public boolean isValid() {
        return userNo != null && jobNo != null;
    }
}