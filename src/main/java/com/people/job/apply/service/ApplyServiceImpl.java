package com.people.job.apply.service;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.entity.ApplyEntity;
import com.people.job.apply.repository.ApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private final ApplyRepository applyRepository;

    @Override
    public void applyToJob(ApplyDTO dto) {
        boolean alreadyApplied = applyRepository.existsByResumeNoAndJobNo(
                dto.getResumeNo(), dto.getJobNo()
        );
        if (alreadyApplied) {
            throw new IllegalStateException("이미 지원한 공고입니다.");
        }

        ApplyEntity entity = ApplyEntity.builder()
                .resumeNo(dto.getResumeNo())
                .jobNo(dto.getJobNo())
                .userNo(dto.getUserNo()) // 없으면 인증정보에서 꺼내거나 DTO에 추가
                .applyDate(LocalDate.now())
                .status("PENDING")       // 선택
                .message(dto.getMessage()) // 선택
                .build();

        applyRepository.save(entity);
    }

    @Override
    public List<ApplyDTO> getAppliesByResume(Long resumeNo) {
        return applyRepository.findByResumeNo(resumeNo).stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplyDTO> getAppliesByJobopening(Long jobopeningNo) {
        return applyRepository.findByJobNo(jobopeningNo).stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelApply(Long applyNo) {
        applyRepository.deleteById(applyNo);
    }

    private ApplyDTO entityToDTO(ApplyEntity e) {
        return ApplyDTO.builder()
                .applyNo(e.getApplyNo())
                .resumeNo(e.getResumeNo())
                .jobNo(e.getJobNo())
                .userNo(e.getUserNo())
                .applyDate(e.getApplyDate()) //
                .status(e.getStatus())
                .message(e.getMessage())
                .build();
    }
}
