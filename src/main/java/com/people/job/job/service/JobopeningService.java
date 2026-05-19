// D:\peoplejob-backend\src\main\java\com\people\job\job\service\JobopeningService.java
package com.people.job.job.service;

import com.people.job.job.dto.JobopeningDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface JobopeningService {

    JobopeningDTO create(JobopeningDTO dto);
    JobopeningDTO getById(Long jobNo);
    JobopeningDTO update(Long jobNo, JobopeningDTO dto, Long userNo);
    void delete(Long jobNo, Long userNo);
    Page<JobopeningDTO> getAll(Pageable pageable);
    Page<JobopeningDTO> getByUser(Long userNo, Pageable pageable);


    /**
     * 임시저장
     */
    JobopeningDTO saveDraft(JobopeningDTO dto, Long userNo);

    /**
     * 임시저장된 채용공고 목록 조회
     */
    Page<JobopeningDTO> getDraftsByUser(Long userNo, Pageable pageable);

    /**
     * 게시 (임시저장 -> 게시중)
     */
    JobopeningDTO publish(Long jobNo, Long userNo);

    /**
     * 게시중인 채용공고만 조회 (일반 사용자용)
     */
    Page<JobopeningDTO> getPublishedJobs(Pageable pageable);

    /**
     * 사용자별 상태별 채용공고 조회
     */
    Page<JobopeningDTO> getJobsByStatus(Long userNo, String status, Pageable pageable);

    /**
     * 사용자의 채용공고 상태별 개수
     */
    Map<String, Long> getJobStatusCounts(Long userNo);

    /**
     * 채용공고 상태 변경
     */
    JobopeningDTO changeStatus(Long jobNo, String status, Long userNo);

    /**
     * 마감일 지난 채용공고들 자동 마감 처리
     */
    void expireOverdueJobs();

    /**
     * 검색 (게시중인 것만)
     */
    Page<JobopeningDTO> searchJobs(String keyword, Pageable pageable);

    /**
     * 카테고리별 조회 (게시중인 것만)
     */
    Page<JobopeningDTO> getJobsByCategory(String jobType, String location, Pageable pageable);
}