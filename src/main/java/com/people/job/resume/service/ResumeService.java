package com.people.job.resume.service;

import com.people.job.resume.dto.ResumeDTO;

import java.util.List;

public interface ResumeService {

    Long insertResume(ResumeDTO dto);

    List<ResumeDTO> selectAll();

    ResumeDTO selectByNo(Long resumeNo);

    void updateResume(ResumeDTO dto);

    void deleteResume(Long resumeNo);

    List<ResumeDTO> selectByUserNo(Long userNo);

    List<ResumeDTO> searchResumes(String keyword);
    List<ResumeDTO> getResumesByJobType(String jobType);
    List<ResumeDTO> getResumesByLocation(String location);
}
