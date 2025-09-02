package com.people.job.job.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobopening")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobopeningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_no")
    private Long jobNo;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String company;

    private String location;
    private String jobType;
    private String salary;
    private String workType;
    private String experience;
    private String education;

    private LocalDate deadline;

    @Column(nullable = false, updatable = false)
    private LocalDateTime regdate;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Integer viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.DRAFT;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Long userNo;

    public enum JobStatus {
        DRAFT("임시저장"),           // 작성 중 (임시저장)
        PENDING("승인대기"),         // 관리자 승인 대기  
        PUBLISHED("게시중"),        // 게시 중
        EXPIRED("마감"),           // 마감
        REJECTED("승인거부"),       // 관리자 승인 거부
        SUSPENDED("게시중단");      // 관리자가 중단한 상태

        private final String description;

        JobStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @PrePersist
    protected void onCreate() {
        this.regdate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isDraft() {
        return this.status == JobStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == JobStatus.PUBLISHED;
    }

    public boolean isExpired() {
        return this.status == JobStatus.EXPIRED;
    }

    public boolean canBeEdited() {
        return this.status == JobStatus.DRAFT || this.status == JobStatus.REJECTED;
    }

    public boolean canBePublished() {
        return this.status == JobStatus.DRAFT || this.status == JobStatus.REJECTED;
    }

    public void publish() {
        if (canBePublished()) {
            this.status = JobStatus.PUBLISHED;
        } else {
            throw new IllegalStateException("현재 상태에서는 게시할 수 없습니다.");
        }
    }

    public void expire() {
        if (this.status == JobStatus.PUBLISHED) {
            this.status = JobStatus.EXPIRED;
        }
    }

    public void suspend() {
        if (this.status == JobStatus.PUBLISHED) {
            this.status = JobStatus.SUSPENDED;
        }
    }

    public void reject() {
        if (this.status == JobStatus.PENDING) {
            this.status = JobStatus.REJECTED;
        }
    }

    public void saveDraft() {
        this.status = JobStatus.DRAFT;
    }

    // 마감일 확인
    public boolean isDeadlinePassed() {
        if (deadline == null) return false;
        return LocalDate.now().isAfter(deadline);
    }
}