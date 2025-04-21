package com.people.job.resume.repository;

import com.people.job.resume.entity.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
    List<ResumeEntity> findByUserNo(Long userNo); // 마이페이지용
}
