package com.people.job.job.controller;

import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.service.JobopeningService;
import com.people.job.user.repository.UserRepository;
import com.people.job.user.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private Long extractUserNo(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new RuntimeException("인증이 필요합니다.");
        }
        String userid = jwtTokenProvider.getUserid(auth.substring(7));
        return userRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."))
                .getUserNo();
    }

    // 기존 메서드들...

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody JobopeningDTO dto, HttpServletRequest request) {
        try {
            dto.setUserNo(extractUserNo(request));
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
            @RequestBody JobopeningDTO dto,
            HttpServletRequest request
    ) {
        try {
            Long userNo = extractUserNo(request);
            JobopeningDTO updated = jobopeningService.update(jobNo, dto, userNo);
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
    public ResponseEntity<?> deleteJob(@PathVariable Long jobNo, HttpServletRequest request) {
        try {
            Long userNo = extractUserNo(request);
            jobopeningService.delete(jobNo, userNo);
            return ResponseEntity.ok(Map.of("message", "채용공고가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("채용공고 삭제 실패 - jobNo: {}", jobNo, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ 새로 추가: 상태 관리 API들

    @PostMapping("/draft")
    public ResponseEntity<?> saveDraft(@RequestBody JobopeningDTO dto, HttpServletRequest request) {
        try {
            Long userNo = extractUserNo(request);
            JobopeningDTO saved = jobopeningService.saveDraft(dto, userNo);
            return ResponseEntity.ok(Map.of(
                    "message", "채용공고가 임시저장되었습니다.",
                    "job", saved
            ));
        } catch (Exception e) {
            log.error("채용공고 임시저장 실패", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/my/drafts")
    public ResponseEntity<?> getMyDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        try {
            Long userNo = extractUserNo(request);
            Pageable pageable = PageRequest.of(page, size);
            Page<JobopeningDTO> drafts = jobopeningService.getDraftsByUser(userNo, pageable);
            return ResponseEntity.ok(drafts);
        } catch (Exception e) {
            log.error("임시저장 목록 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{jobNo}/publish")
    public ResponseEntity<?> publishJob(
            @PathVariable Long jobNo,
            HttpServletRequest request
    ) {
        try {
            Long userNo = extractUserNo(request);
            JobopeningDTO published = jobopeningService.publish(jobNo, userNo);
            return ResponseEntity.ok(Map.of(
                    "message", "채용공고가 게시되었습니다.",
                    "job", published
            ));
        } catch (Exception e) {
            log.error("채용공고 게시 실패 - jobNo: {}", jobNo, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/my")
    public ResponseEntity<?> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            HttpServletRequest request
    ) {
        try {
            Long userNo = extractUserNo(request);
            Pageable pageable = PageRequest.of(page, size);
            Page<JobopeningDTO> jobs = (status != null && !status.isEmpty())
                    ? jobopeningService.getJobsByStatus(userNo, status, pageable)
                    : jobopeningService.getByUser(userNo, pageable);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            log.error("내 채용공고 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/my/status-counts")
    public ResponseEntity<?> getMyJobStatusCounts(HttpServletRequest request) {
        try {
            Long userNo = extractUserNo(request);
            Map<String, Long> counts = jobopeningService.getJobStatusCounts(userNo);
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            log.error("내 채용공고 상태별 개수 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{jobNo}/status")
    public ResponseEntity<?> changeJobStatus(
            @PathVariable Long jobNo,
            @RequestParam String status,
            HttpServletRequest request
    ) {
        try {
            Long userNo = extractUserNo(request);
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