package com.people.job.job.service;

import com.people.job.job.dto.JobopeningDTO;

import java.util.List;

public interface JobopeningService {

    void insertJobopening(JobopeningDTO dto);

    List<JobopeningDTO> selectAll();

    JobopeningDTO selectByNo(Long jobopeningNo);

    void updateJobopening(JobopeningDTO dto);

    void deleteJobopening(Long jobopeningNo);
}
