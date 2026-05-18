package com.people.job.apply.service;

import com.people.job.apply.dto.ApplyDTO;

import java.util.List;

public interface ApplyService {

    void applyToJob(ApplyDTO dto);

    List<ApplyDTO> getAppliesByResume(Long resumeNo);

    List<ApplyDTO> getAppliesByJobopening(Long jobopeningNo);

    void cancelApply(Long applyNo);

    void updateStatus(Long applyNo, String status);

    boolean hasApplied(Long userNo, Long jobNo);
}
