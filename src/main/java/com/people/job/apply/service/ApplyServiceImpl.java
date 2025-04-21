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
        boolean alreadyApplied = applyRepository.existsByResumeNoAndJobopeningNo(
                dto.getResumeNo(), dto.getJobopeningNo()
        );

        if (alreadyApplied) {
            throw new RuntimeException("이미 지원한 공고입니다.");
        }

        ApplyEntity entity = ApplyEntity.builder()
                .resumeNo(dto.getResumeNo())
                .jobopeningNo(dto.getJobopeningNo())
                .regdate(LocalDate.now())
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
        return applyRepository.findByJobopeningNo(jobopeningNo).stream()
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
                .jobopeningNo(e.getJobopeningNo())
                .regdate(e.getRegdate())
                .build();
    }
}
