package com.people.job.job.repository;

import com.people.job.job.entity.JobopeningEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobopeningRepository extends JpaRepository<JobopeningEntity, Long> {
    // 필요한 경우: List<JobopeningEntity> findByTitleContaining(String keyword);
}
