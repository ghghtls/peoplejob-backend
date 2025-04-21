package com.people.job.mypage.service;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.resume.dto.ResumeDTO;

import java.util.List;

public interface MypageService {

    // 개인회원용
    List<ResumeDTO> getMyResumes(Long userNo);
    List<ApplyDTO> getMyApplies(Long userNo);

    // 기업회원용
    List<JobopeningDTO> getMyJobopenings(Long companyNo);
    List<ApplyDTO> getAppliesForMyJob(Long jobopeningNo);
}
