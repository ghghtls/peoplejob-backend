package com.people.job.apply.service;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.entity.ApplyEntity;
import com.people.job.apply.repository.ApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplyServiceImpl implements ApplyService {

    private final ApplyRepository applyRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void applyToJob(ApplyDTO dto) {
        if (dto.getResumeNo() == null) {
            throw new IllegalArgumentException("resumeNo는 필수입니다.");
        }
        boolean alreadyApplied = applyRepository.existsByUserNoAndJobNo(
                dto.getUserNo(), dto.getJobNo()
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
    @Transactional(readOnly = true)
    public List<ApplyDTO> getAppliesByResume(Long resumeNo) {
        return applyRepository.findByResumeNo(resumeNo).stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplyDTO> getAppliesByJobopening(Long jobopeningNo) {
        return applyRepository.findByJobNo(jobopeningNo).stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelApply(Long applyNo) {
        applyRepository.deleteById(applyNo);
    }

    @Override
    public void updateStatus(Long applyNo, String status) {
        ApplyEntity entity = applyRepository.findById(applyNo)
                .orElseThrow(() -> new RuntimeException("지원 내역이 존재하지 않습니다."));
        entity.setStatus(status);
        applyRepository.save(entity);
    }

    @Override
    public boolean hasApplied(Long userNo, Long jobNo) {
        return applyRepository.existsByUserNoAndJobNo(userNo, jobNo);
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
