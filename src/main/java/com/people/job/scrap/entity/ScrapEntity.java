package com.people.job.scrap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "scrap",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userNo", "jobNo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scrapNo;

    @Column(nullable = false)
    private Long userNo;

    @Column(nullable = false)
    private Long jobNo; // DB 스키마에 맞춤 (jobopeningNo -> jobNo)

    @Column(nullable = false)
    private LocalDate scrapDate; // DB 스키마에 맞춤 (regdate -> scrapDate)

    @PrePersist
    protected void onCreate() {
        this.scrapDate = LocalDate.now();
    }
}