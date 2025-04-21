package com.people.job.apply.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "apply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applyNo;

    private Long resumeNo;      // 지원한 이력서 번호
    private Long jobopeningNo;  // 지원한 채용공고 번호

    private LocalDate regdate;
}
