package com.people.job.resume.service;

import com.people.job.resume.dto.ResumeDTO;

import java.util.List;

public interface ResumeService {

    void insertResume(ResumeDTO dto);

    List<ResumeDTO> selectAll();

    ResumeDTO selectByNo(Long resumeNo);

    void updateResume(ResumeDTO dto);

    void deleteResume(Long resumeNo);

    List<ResumeDTO> selectByUserNo(Long userNo); // 마이페이지용
}
