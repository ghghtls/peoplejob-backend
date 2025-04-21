package com.people.job.mypage.service;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.entity.ApplyEntity;
import com.people.job.apply.repository.ApplyRepository;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.resume.dto.ResumeDTO;
import com.people.job.resume.entity.ResumeEntity;
import com.people.job.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService {

    private final ResumeRepository resumeRepository;
    private final ApplyRepository applyRepository;
    private final JobopeningRepository jobopeningRepository;

    @Override
    public List<ResumeDTO> getMyResumes(Long userNo) {
        return resumeRepository.findByUserNo(userNo).stream()
                .map(this::toResumeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplyDTO> getMyApplies(Long userNo) {
        List<ResumeEntity> resumes = resumeRepository.findByUserNo(userNo);
        return resumes.stream()
                .flatMap(resume -> applyRepository.findByResumeNo(resume.getResumeNo()).stream())
                .map(this::toApplyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobopeningDTO> getMyJobopenings(Long companyNo) {
        return jobopeningRepository.findAll().stream()
                .filter(job -> job.getCompanyNo().equals(companyNo))
                .map(this::toJobDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplyDTO> getAppliesForMyJob(Long jobopeningNo) {
        return applyRepository.findByJobopeningNo(jobopeningNo).stream()
                .map(this::toApplyDTO)
                .collect(Collectors.toList());
    }

    private ResumeDTO toResumeDTO(ResumeEntity entity) {
        return ResumeDTO.builder()
                .resumeNo(entity.getResumeNo())
                .title(entity.getTitle())
                .content(entity.getContent())
                .education(entity.getEducation())
                .career(entity.getCareer())
                .certificate(entity.getCertificate())
                .hopeJobtype(entity.getHopeJobtype())
                .hopeLocation(entity.getHopeLocation())
                .salary(entity.getSalary())
                .workType(entity.getWorkType())
                .regdate(entity.getRegdate())
                .imagePath(entity.getImagePath())
                .originalImage(entity.getOriginalImage())
                .userNo(entity.getUserNo())
                .build();
    }

    private JobopeningDTO toJobDTO(JobopeningEntity e) {
        return JobopeningDTO.builder()
                .jobopeningNo(e.getJobopeningNo())
                .title(e.getTitle())
                .content(e.getContent())
                .jobtype(e.getJobtype())
                .location(e.getLocation())
                .education(e.getEducation())
                .career(e.getCareer())
                .salary(e.getSalary())
                .regdate(e.getRegdate())
                .deadline(e.getDeadline())
                .companyNo(e.getCompanyNo())
                .filename(e.getFilename())
                .originalFilename(e.getOriginalFilename())
                .build();
    }

    private ApplyDTO toApplyDTO(ApplyEntity e) {
        return ApplyDTO.builder()
                .applyNo(e.getApplyNo())
                .resumeNo(e.getResumeNo())
                .jobopeningNo(e.getJobopeningNo())
                .regdate(e.getRegdate())
                .build();
    }
}
