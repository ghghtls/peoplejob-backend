package com.people.job.job.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.people.job.job.entity.JobopeningEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobopeningDTO {

    private Long jobNo;
    private String title;
    private String content;
    private String company;
    private String location;
    private String jobType;
    private String salary;
    private String workType;
    private String experience;
    private String education;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate deadline;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate regdate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private Integer viewCount;
    @JsonProperty("isActive")
    private Boolean isActive;
    private Long userNo;

    private String status;
    private String statusDescription;
    private Boolean canEdit;
    private Boolean canPublish;
    private Boolean canDelete;
    @JsonProperty("isExpired")
    private Boolean isExpired;
    @JsonProperty("isAdvertised")
    private Boolean isAdvertised;
    private String filename;
    private String originalFilename;

    public static JobopeningDTO fromEntity(JobopeningEntity entity) {
        return JobopeningDTO.builder()
                .jobNo(entity.getJobNo())
                .title(entity.getTitle())
                .content(entity.getContent())
                .company(entity.getCompany())
                .location(entity.getLocation())
                .jobType(entity.getJobType())
                .salary(entity.getSalary())
                .workType(entity.getWorkType())
                .experience(entity.getExperience())
                .education(entity.getEducation())
                .deadline(entity.getDeadline())
                .regdate(LocalDate.from(entity.getRegdate())) // LocalDate 타입
                .updatedAt(entity.getUpdatedAt())
                .viewCount(entity.getViewCount())
                .isActive(entity.getIsActive())
                .userNo(entity.getUserNo())

                // 상태 관리 정보
                .status(entity.getStatus() != null ? entity.getStatus().name() : "DRAFT")
                .statusDescription(entity.getStatus() != null ? entity.getStatus().getDescription() : "임시저장")
                .canEdit(entity.canBeEdited())
                .canPublish(entity.canBePublished())
                .canDelete(entity.isDraft() || (entity.getStatus() != null && entity.getStatus() == JobopeningEntity.JobStatus.REJECTED))
                .isExpired(entity.isDeadlinePassed())
                .isAdvertised(entity.getIsAdvertised() != null && entity.getIsAdvertised())
                .filename(entity.getFilename())
                .originalFilename(entity.getOriginalFilename())
                .build();
    }

    public JobopeningEntity toEntity() {
        return JobopeningEntity.builder()
                .jobNo(this.jobNo)
                .title(this.title)
                .content(this.content)
                .company(this.company)
                .location(this.location)
                .jobType(this.jobType)
                .salary(this.salary)
                .workType(this.workType)
                .experience(this.experience)
                .education(this.education)
                .deadline(this.deadline)
                .viewCount(this.viewCount != null ? this.viewCount : 0)
                .isActive(this.isActive != null ? this.isActive : true)
                .userNo(this.userNo)
                .status(this.status != null ?
                        JobopeningEntity.JobStatus.valueOf(this.status) :
                        JobopeningEntity.JobStatus.DRAFT)
                .isAdvertised(this.isAdvertised != null ? this.isAdvertised : false)
                .filename(this.filename)
                .originalFilename(this.originalFilename)
                .build();
    }
}