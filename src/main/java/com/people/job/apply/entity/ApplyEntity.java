package com.people.job.apply.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "apply",
        uniqueConstraints = @UniqueConstraint(name = "uk_apply_user_job", columnNames = {"user_no", "job_no"}),
        indexes = {
                @Index(name = "idx_apply_user_no",   columnList = "user_no"),
                @Index(name = "idx_apply_job_no",    columnList = "job_no"),
                @Index(name = "idx_apply_resume_no", columnList = "resume_no")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apply_no")           // ★ PK 컬럼명 매핑
    private Long applyNo;

    @Column(name = "job_no", nullable = false)
    private Long jobNo;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "resume_no", nullable = false)
    private Long resumeNo;

    @Column(name = "apply_date", nullable = false)
    private LocalDate applyDate;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @PrePersist
    protected void onCreate() {
        this.applyDate = (this.applyDate == null) ? LocalDate.now() : this.applyDate;
        if (this.status == null) this.status = "PENDING";
    }
}
