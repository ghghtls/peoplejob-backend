package com.people.job.apply.controller;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.entity.ApplyEntity;
import com.people.job.apply.repository.ApplyRepository;
import com.people.job.apply.service.ApplyService;
import com.people.job.common.ApiResponse;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.user.repository.UserRepository;
import com.people.job.user.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyService applyService;
    private final ApplyRepository applyRepository;
    private final JobopeningRepository jobopeningRepository;
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

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> apply(@RequestBody ApplyDTO dto) {
        applyService.applyToJob(dto);
        return ResponseEntity.ok(ApiResponse.ok("지원이 완료되었습니다."));
    }

    @GetMapping("/resume/{resumeNo}")
    public ResponseEntity<ApiResponse<List<ApplyDTO>>> getByResume(@PathVariable Long resumeNo) {
        return ResponseEntity.ok(ApiResponse.success(applyService.getAppliesByResume(resumeNo)));
    }

    @GetMapping("/job/{jobopeningNo}")
    public ResponseEntity<ApiResponse<List<ApplyDTO>>> getByJob(
            @PathVariable Long jobopeningNo,
            HttpServletRequest request) {
        Long userNo = extractUserNo(request);
        Long jobOwner = jobopeningRepository.findById(jobopeningNo)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."))
                .getUserNo();
        if (!jobOwner.equals(userNo)) {
            return ResponseEntity.status(403).body(ApiResponse.error("권한이 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(applyService.getAppliesByJobopening(jobopeningNo)));
    }

    @DeleteMapping("/{applyNo}")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long applyNo) {
        applyService.cancelApply(applyNo);
        return ResponseEntity.ok(ApiResponse.ok("지원이 취소되었습니다."));
    }

    @PutMapping("/{applyNo}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long applyNo,
            @RequestParam String status,
            HttpServletRequest request) {
        Long userNo = extractUserNo(request);
        ApplyEntity apply = applyRepository.findById(applyNo)
                .orElseThrow(() -> new RuntimeException("지원 내역이 존재하지 않습니다."));
        Long jobOwner = jobopeningRepository.findById(apply.getJobNo())
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다."))
                .getUserNo();
        if (!jobOwner.equals(userNo)) {
            return ResponseEntity.status(403).body(ApiResponse.error("권한이 없습니다."));
        }
        applyService.updateStatus(applyNo, status);
        return ResponseEntity.ok(ApiResponse.ok("상태가 변경되었습니다."));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkApply(
            @RequestParam Integer jobNo,
            @RequestParam Integer userNo) {
        boolean applied = applyRepository.existsByJobNoAndUserNo(jobNo.longValue(), userNo.longValue());
        return ResponseEntity.ok(ApiResponse.success(applied));
    }
}
