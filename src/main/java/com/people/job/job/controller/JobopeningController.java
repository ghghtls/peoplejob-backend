package com.people.job.job.controller;

import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.service.JobopeningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobopeningController {

    private final JobopeningService jobopeningService;

    // 기존 메서드들...

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody JobopeningDTO dto) {
        try {
            JobopeningDTO created = jobopeningService.create(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "채용공고가 생성되었습니다.",
                    "job", created
            ));
        } catch (Exception e) {
            log.error("채용공고 생성 실패", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping
    public ResponseEntity<?> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<JobopeningDTO> jobs;

            if ("published".equals(status)) {
                jobs = jobopeningService.getPublishedJobs(pageable);
            } else {
                jobs = jobopeningService.getAll(pageable);
            }

            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            log.error("채용공고 목록 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{jobNo}")
    public ResponseEntity<?> getJobById(@PathVariable Long jobNo) {
        try {
            JobopeningDTO job = jobopeningService.getById(jobNo);
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            log.error("채용공고 조회 실패 - jobNo: {}", jobNo, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{jobNo}")
    public ResponseEntity<?> updateJob(
            @PathVariable Long jobNo,
            @RequestBody JobopeningDTO dto
    ) {
        try {
            JobopeningDTO updated = jobopeningService.update(jobNo, dto);
            return ResponseEntity.ok(Map.of(
                    "message", "채용공고가 수정되었습니다.",
                    "job", updated
            ));
        } catch (Exception e) {
            log.error("채용공고 수정 실패 - jobNo: {}", jobNo, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{jobNo}")
    public ResponseEntity<?> deleteJob(@PathVariable Long jobNo) {
        try {
            jobopeningService.delete(jobNo);
            return ResponseEntity.ok(Map.of("message", "채용공고가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("채용공고 삭제 실패 - jobNo: {}", jobNo, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ 새로 추가: 상태 관리 API들

    @PostMapping("/draft")
    public ResponseEntity<?> saveDraft(@RequestBody JobopeningDTO dto) {
        try {
            JobopeningDTO saved = jobopeningService.saveDraft(dto);
            return ResponseEntity.ok(Map.of(
                    "message", "채용공고가 임시저장되었습니다.",
                    "job", saved
            ));
        } catch (Exception e) {
            log.error("채용공고 임시저장 실패", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userNo}/drafts")
    public ResponseEntity<?> getUserDrafts(
            @PathVariable Long userNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<JobopeningDTO> drafts = jobopeningService.getDraftsByUser(userNo, pageable);
            return ResponseEntity.ok(drafts);
        } catch (Exception e) {
            log.error("임시저장 목록 조회 실패 - userNo: {}", userNo, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{jobNo}/publish")
    public ResponseEntity<?> publishJob(
            @PathVariable Long jobNo,
            @RequestParam Long userNo
    ) {
        try {
            JobopeningDTO published = jobopeningService.publish(jobNo, userNo);
            return ResponseEntity.ok(Map.of(
                    "message", "채용공고가 게시되었습니다.",
                    "job", published
            ));
        } catch (Exception e) {
            log.error("채용공고 게시 실패 - jobNo: {}, userNo: {}", jobNo, userNo, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userNo}")
    public ResponseEntity<?> getUserJobs(
            @PathVariable Long userNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<JobopeningDTO> jobs;

            if (status != null && !status.isEmpty()) {
                jobs = jobopeningService.getJobsByStatus(userNo, status, pageable);
            } else {
                jobs = jobopeningService.getByUser(userNo, pageable);
            }

            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            log.error("사용자 채용공고 조회 실패 - userNo: {}, status: {}", userNo, status, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userNo}/status-counts")
    public ResponseEntity<?> getUserJobStatusCounts(@PathVariable Long userNo) {
        try {
            Map<String, Long> counts = jobopeningService.getJobStatusCounts(userNo);
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            log.error("사용자 채용공고 상태별 개수 조회 실패 - userNo: {}", userNo, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{jobNo}/status")
    public ResponseEntity<?> changeJobStatus(
            @PathVariable Long jobNo,
            @RequestParam String status,
            @RequestParam Long userNo
    ) {
        try {
            JobopeningDTO updated = jobopeningService.changeStatus(jobNo, status, userNo);
            return ResponseEntity.ok(Map.of(
                    "message", "채용공고 상태가 변경되었습니다.",
                    "job", updated
            ));
        } catch (Exception e) {
            log.error("채용공고 상태 변경 실패 - jobNo: {}, status: {}", jobNo, status, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<JobopeningDTO> jobs = jobopeningService.searchJobs(keyword, pageable);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            log.error("채용공고 검색 실패 - keyword: {}", keyword, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/category")
    public ResponseEntity<?> getJobsByCategory(
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<JobopeningDTO> jobs = jobopeningService.getJobsByCategory(jobType, location, pageable);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            log.error("카테고리별 채용공고 조회 실패 - jobType: {}, location: {}", jobType, location, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ 관리자용 API들
    @PostMapping("/{jobNo}/expire")
    public ResponseEntity<?> expireJob(@PathVariable Long jobNo) {
        try {
            JobopeningDTO expired = jobopeningService.changeStatus(jobNo, "EXPIRED", null);
            return ResponseEntity.ok(Map.of(
                    "message", "채용공고가 마감 처리되었습니다.",
                    "job", expired
            ));
        } catch (Exception e) {
            log.error("채용공고 마감 처리 실패 - jobNo: {}", jobNo, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/expire-overdue")
    public ResponseEntity<?> expireOverdueJobs() {
        try {
            jobopeningService.expireOverdueJobs();
            return ResponseEntity.ok(Map.of("message", "마감일이 지난 채용공고들이 처리되었습니다."));
        } catch (Exception e) {
            log.error("마감일 지난 채용공고 처리 실패", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}