package com.people.job.apply.repository;

import com.people.job.apply.entity.ApplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplyRepository extends JpaRepository<ApplyEntity, Long> {

    boolean existsByResumeNoAndJobopeningNo(Long resumeNo, Long jobopeningNo);

    List<ApplyEntity> findByResumeNo(Long resumeNo);

    List<ApplyEntity> findByJobopeningNo(Long jobopeningNo);

    Optional<ApplyEntity> findByResumeNoAndJobopeningNo(Long resumeNo, Long jobopeningNo);
}
