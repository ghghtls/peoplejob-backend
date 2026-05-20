package com.people.job.job.service;

import com.people.job.job.repository.JobopeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsyncViewCountService {

    private final JobopeningRepository jobRepository;

    @Async("taskExecutor")
    @Transactional
    public void increment(Long jobNo) {
        jobRepository.findByJobNoAndIsActiveTrue(jobNo).ifPresent(entity -> {
            entity.setViewCount(entity.getViewCount() + 1);
            jobRepository.save(entity);
        });
    }
}
