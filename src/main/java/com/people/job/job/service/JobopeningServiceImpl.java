package com.people.job.job.service;

import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JobopeningServiceImpl implements JobopeningService {

    private final JobopeningRepository jobRepository;

    @Override
    public JobopeningDTO create(JobopeningDTO dto) {
        // 기본적으로 임시저장 상태로 생성
        dto.setStatus("DRAFT");
        JobopeningEntity entity = dto.toEntity();
        JobopeningEntity saved = jobRepository.save(entity);

        log.info("채용공고 생성 완료 - jobNo: {}, status: {}", saved.getJobNo(), saved.getStatus());
        return JobopeningDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public JobopeningDTO getById(Long jobNo) {
        JobopeningEntity entity = jobRepository.findByJobNoAndIsActiveTrue(jobNo)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."));

        // 게시중인 경우에만 조회수 증가 (임시저장은 조회수 증가 안함)
        if (entity.isPublished()) {
            entity.setViewCount(entity.getViewCount() + 1);
            jobRepository.save(entity);
        }

        return JobopeningDTO.fromEntity(entity);
    }

    @Override
    public JobopeningDTO update(Long jobNo, JobopeningDTO dto) {
        JobopeningEntity entity = jobRepository.findByJobNoAndIsActiveTrue(jobNo)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."));

        // 수정 가능한 상태인지 확인
        if (!entity.canBeEdited()) {
            throw new RuntimeException("현재 상태에서는 수정할 수 없습니다.");
        }

        // 필드 업데이트
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setCompany(dto.getCompany());
        entity.setLocation(dto.getLocation());
        entity.setJobType(dto.getJobType());
        entity.setSalary(dto.getSalary());
        entity.setWorkType(dto.getWorkType());
        entity.setExperience(dto.getExperience());
        entity.setEducation(dto.getEducation());
        entity.setDeadline(dto.getDeadline());

        JobopeningEntity updated = jobRepository.save(entity);
        log.info("채용공고 수정 완료 - jobNo: {}", jobNo);

        return JobopeningDTO.fromEntity(updated);
    }

    @Override
    public void delete(Long jobNo) {
        JobopeningEntity entity = jobRepository.findByJobNoAndIsActiveTrue(jobNo)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."));

        // 삭제 가능한 상태인지 확인 (임시저장 또는 승인거부 상태만)
        if (!entity.isDraft() && entity.getStatus() != JobopeningEntity.JobStatus.REJECTED) {
            throw new RuntimeException("현재 상태에서는 삭제할 수 없습니다.");
        }

        entity.setIsActive(false);
        jobRepository.save(entity);

        log.info("채용공고 삭제 완료 - jobNo: {}", jobNo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobopeningDTO> getAll(Pageable pageable) {
        // 관리자용 - 모든 채용공고 조회
        return jobRepository.findByIsActiveTrueOrderByRegdateDesc(pageable)
                .map(JobopeningDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobopeningDTO> getByUser(Long userNo, Pageable pageable) {
        return jobRepository.findByUserNoAndIsActiveTrueOrderByRegdateDesc(userNo, pageable)
                .map(JobopeningDTO::fromEntity);
    }


    @Override
    public JobopeningDTO saveDraft(JobopeningDTO dto) {
        if (dto.getJobNo() != null) {
            // 기존 임시저장 수정
            return update(dto.getJobNo(), dto);
        } else {
            // 새로운 임시저장 생성
            dto.setStatus("DRAFT");
            return create(dto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobopeningDTO> getDraftsByUser(Long userNo, Pageable pageable) {
        return jobRepository.findDraftsByUser(userNo, pageable)
                .map(JobopeningDTO::fromEntity);
    }

    @Override
    public JobopeningDTO publish(Long jobNo, Long userNo) {
        JobopeningEntity entity = jobRepository.findByJobNoAndIsActiveTrue(jobNo)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."));

        // 권한 확인
        if (!entity.getUserNo().equals(userNo)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        // 필수 필드 검증
        validateRequiredFields(entity);

        // 게시 처리
        entity.publish();
        JobopeningEntity published = jobRepository.save(entity);

        log.info("채용공고 게시 완료 - jobNo: {}, userNo: {}", jobNo, userNo);
        return JobopeningDTO.fromEntity(published);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobopeningDTO> getPublishedJobs(Pageable pageable) {
        return jobRepository.findPublishedJobs(pageable)
                .map(JobopeningDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobopeningDTO> getJobsByStatus(Long userNo, String status, Pageable pageable) {
        JobopeningEntity.JobStatus jobStatus = JobopeningEntity.JobStatus.valueOf(status.toUpperCase());
        return jobRepository.findByUserNoAndStatusAndIsActiveTrueOrderByRegdateDesc(userNo, jobStatus, pageable)
                .map(JobopeningDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getJobStatusCounts(Long userNo) {
        Map<String, Long> counts = new HashMap<>();

        for (JobopeningEntity.JobStatus status : JobopeningEntity.JobStatus.values()) {
            long count = jobRepository.countByUserNoAndStatus(userNo, status);
            counts.put(status.name(), count);
        }

        return counts;
    }

    @Override
    public JobopeningDTO changeStatus(Long jobNo, String status, Long userNo) {
        JobopeningEntity entity = jobRepository.findByJobNoAndIsActiveTrue(jobNo)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."));

        // 권한 확인
        if (!entity.getUserNo().equals(userNo)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        JobopeningEntity.JobStatus newStatus = JobopeningEntity.JobStatus.valueOf(status.toUpperCase());

        // 상태 변경 로직
        switch (newStatus) {
            case PUBLISHED -> entity.publish();
            case EXPIRED -> entity.expire();
            case SUSPENDED -> entity.suspend();
            case DRAFT -> entity.saveDraft();
            default -> throw new RuntimeException("지원하지 않는 상태 변경입니다.");
        }

        JobopeningEntity updated = jobRepository.save(entity);
        log.info("채용공고 상태 변경 완료 - jobNo: {}, status: {} -> {}", jobNo, entity.getStatus(), newStatus);

        return JobopeningDTO.fromEntity(updated);
    }

    @Override
    public void expireOverdueJobs() {
        List<JobopeningEntity> expiredJobs = jobRepository.findExpiredJobs(LocalDate.now());

        for (JobopeningEntity job : expiredJobs) {
            job.expire();
            jobRepository.save(job);
            log.info("채용공고 자동 마감 처리 - jobNo: {}, deadline: {}", job.getJobNo(), job.getDeadline());
        }

        if (!expiredJobs.isEmpty()) {
            log.info("총 {}개의 채용공고가 자동 마감 처리되었습니다.", expiredJobs.size());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobopeningDTO> searchJobs(String keyword, Pageable pageable) {
        return jobRepository.searchPublishedJobs(keyword, pageable)
                .map(JobopeningDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobopeningDTO> getJobsByCategory(String jobType, String location, Pageable pageable) {
        return jobRepository.findPublishedJobsByCategory(jobType, location, pageable)
                .map(JobopeningDTO::fromEntity);
    }


    private void validateRequiredFields(JobopeningEntity entity) {
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            throw new RuntimeException("제목을 입력해주세요.");
        }
        if (entity.getContent() == null || entity.getContent().trim().isEmpty()) {
            throw new RuntimeException("내용을 입력해주세요.");
        }
        if (entity.getCompany() == null || entity.getCompany().trim().isEmpty()) {
            throw new RuntimeException("회사명을 입력해주세요.");
        }
        if (entity.getDeadline() == null) {
            throw new RuntimeException("마감일을 설정해주세요.");
        }
        if (entity.getDeadline().isBefore(LocalDate.now())) {
            throw new RuntimeException("마감일은 오늘 이후여야 합니다.");
        }
    }
}