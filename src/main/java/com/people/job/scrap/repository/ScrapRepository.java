package com.people.job.scrap.repository;

import com.people.job.scrap.entity.ScrapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<ScrapEntity, Long> {

    boolean existsByUserNoAndJobopeningNo(Long userNo, Long jobopeningNo);

    List<ScrapEntity> findByUserNo(Long userNo);

    Optional<ScrapEntity> findByUserNoAndJobopeningNo(Long userNo, Long jobopeningNo);
}
