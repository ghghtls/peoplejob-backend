package com.people.job.job.service;

import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final UserRepository userRepository;

    @Override
    @CacheEvict(value = "publishedJobs", allEntries = true)
    public JobopeningDTO create(JobopeningDTO dto) {
        // company가 없으면 userNo로 회사명 자동 조회
        if ((dto.getCompany() == null || dto.getCompany().isBlank()) && dto.getUserNo() != null) {
            userRepository.findById(dto.getUserNo()).ifPresent(user -> {
                String name = user.getCompanyName() != null ? user.getCompanyName() : user.getUsername();
                dto.setCompany(name != null ? name : "미등록");
            });
        }
        if (dto.getCompany() == null || dto.getCompany().isBlank()) {
            dto.setCompany("미등록");
        }

        dto.setStatus("DRAFT");
        JobopeningEntity entity = dto.toEntity();
        JobopeningEntity saved = jobRepository.save(entity);

        log.info("채용공고 생성 완료 - jobNo: {}, status: {}", saved.getJobNo(), saved.getStatus());
        return JobopeningDTO.fromEntity(saved);
    }

    @Override
    @Transactional  // readOnly 제거 — 조회수 write가 포함되어 있음
    public JobopeningDTO getById(Long jobNo) {
        JobopeningEntity entity = jobRepository.findByJobNoAndIsActiveTrue(jobNo)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."));

        if (entity.isPublished()) {
            entity.setViewCount(entity.getViewCount() + 1);
            jobRepository.save(entity);
        }

        return JobopeningDTO.fromEntity(entity);
    }

    @Override
    @CacheEvict(value = "publishedJobs", allEntries = true)
    public JobopeningDTO update(Long jobNo, JobopeningDTO dto) {
        JobopeningEntity entity = jobRepository.findByJobNoAndIsActiveTrue(jobNo)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."));

        // 관리자가 중단한 경우만 수정 불가
        if (entity.getStatus() == JobopeningEntity.JobStatus.SUSPENDED) {
            throw new RuntimeException("관리자에 의해 중단된 채용공고는 수정할 수 없습니다.");
        }

        // 필드 업데이트
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        if (dto.getCompany() != null && !dto.getCompany().isBlank()) {
            entity.setCompany(dto.getCompany());
        }
        entity.setLocation(dto.getLocation());
        entity.setJobType(dto.getJobType());
        entity.setSalary(dto.getSalary());
        entity.setWorkType(dto.getWorkType());
        entity.setExperience(dto.getExperience());
        entity.setEducation(dto.getEducation());
        entity.setDeadline(dto.getDeadline());
        if (dto.getFilename() != null) entity.setFilename(dto.getFilename());
        if (dto.getOriginalFilename() != null) entity.setOriginalFilename(dto.getOriginalFilename());

        JobopeningEntity updated = jobRepository.save(entity);
        log.info("채용공고 수정 완료 - jobNo: {}", jobNo);

        return JobopeningDTO.fromEntity(updated);
    }

    @Override
    @CacheEvict(value = "publishedJobs", allEntries = true)
    public void delete(Long jobNo) {
        JobopeningEntity entity = jobRepository.findByJobNoAndIsActiveTrue(jobNo)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."));

        // 관리자가 중단한 경우만 삭제 불가
        if (entity.getStatus() == JobopeningEntity.JobStatus.SUSPENDED) {
            throw new RuntimeException("관리자에 의해 중단된 채용공고는 삭제할 수 없습니다.");
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
    @CacheEvict(value = "publishedJobs", allEntries = true)
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
    @Cacheable(value = "publishedJobs", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
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
    @CacheEvict(value = "publishedJobs", allEntries = true)
    public JobopeningDTO changeStatus(Long jobNo, String status, Long userNo) {
        JobopeningEntity entity = jobRepository.findByJobNoAndIsActiveTrue(jobNo)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."));

        // 권한 확인 — userNo null이면 관리자 호출(스케줄러/admin API)로 간주
        if (userNo != null && !entity.getUserNo().equals(userNo)) {
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
    @CacheEvict(value = "publishedJobs", allEntries = true)
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
        try {
            // FULLTEXT INDEX가 있으면 대용량에서 10배+ 빠름
            // 없으면 catch → LIKE 검색으로 자동 폴백
            return jobRepository.fullTextSearchPublishedJobs(keyword, pageable)
                    .map(JobopeningDTO::fromEntity);
        } catch (Exception e) {
            log.warn("FULLTEXT 검색 실패 → LIKE 검색으로 폴백 (ALTER TABLE jobopening ADD FULLTEXT INDEX ft_job_search (title, content, company); 실행 필요): {}", e.getMessage());
            return jobRepository.searchPublishedJobs(keyword, pageable)
                    .map(JobopeningDTO::fromEntity);
        }
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