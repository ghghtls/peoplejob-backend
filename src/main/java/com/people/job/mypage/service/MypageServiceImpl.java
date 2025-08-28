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

import java.time.LocalDate;
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
        // 내 이력서들 → 각 이력서의 지원내역
        List<ResumeEntity> resumes = resumeRepository.findByUserNo(userNo);
        return resumes.stream()
                .flatMap(r -> applyRepository.findByResumeNo(r.getResumeNo()).stream())
                .map(this::toApplyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobopeningDTO> getMyJobopenings(Long companyUserNo) {
        // 회사 사용자 소유 공고: jobopening.userNo = 회사 사용자 번호
        return jobopeningRepository.findByUserNo(companyUserNo).stream()
                .map(this::toJobDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplyDTO> getAppliesForMyJob(Long jobNo) {
        return applyRepository.findByJobNo(jobNo).stream()
                .map(this::toApplyDTO)
                .collect(Collectors.toList());
    }

    private ResumeDTO toResumeDTO(ResumeEntity e) {
        return ResumeDTO.builder()
                .resumeNo(e.getResumeNo())
                .title(e.getTitle())
                .content(e.getContent())
                .education(e.getEducation())
                .career(e.getCareer())
                .certificate(e.getCertificate())
                .hopeJobtype(e.getHopeJobtype())
                .hopeLocation(e.getHopeLocation())
                .salary(e.getSalary())
                .workType(e.getWorkType())
                .regdate(e.getRegdate())
                .imagePath(e.getImagePath())
                .originalImage(e.getOriginalImage())
                .userNo(e.getUserNo())
                .build();
    }

    private JobopeningDTO toJobDTO(JobopeningEntity e) {
        return JobopeningDTO.builder()
                .jobNo(e.getJobNo())            // jobopeningNo → jobNo
                .title(e.getTitle())
                .content(e.getContent())
                .jobType(e.getJobType())        // jobtype → jobType
                .location(e.getLocation())
                .education(e.getEducation())
                .experience(e.getExperience())  // career → experience
                .salary(e.getSalary())
                .workType(e.getWorkType())
                .deadline(e.getDeadline())
                .regdate(LocalDate.from(e.getRegdate()))
                .company(e.getCompany())        // 스키마상 company(회사명) 컬럼 존재
                .userNo(e.getUserNo())          // 공고 소유자(회사 사용자 번호)
                .viewCount(e.getViewCount())
                .isActive(e.getIsActive())
                .build();
    }

    private ApplyDTO toApplyDTO(ApplyEntity e) {
        return ApplyDTO.builder()
                .applyNo(e.getApplyNo())
                .resumeNo(e.getResumeNo())
                .jobNo(e.getJobNo())            // jobopeningNo → jobNo
                .userNo(e.getUserNo())          // 지원자 userNo (NOT NULL)
                .applyDate(e.getApplyDate())    // regdate → applyDate
                .status(e.getStatus())
                .message(e.getMessage())
                .build();
    }
}
