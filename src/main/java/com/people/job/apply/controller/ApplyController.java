package com.people.job.apply.controller;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.repository.ApplyRepository;
import com.people.job.apply.service.ApplyService;
import com.people.job.common.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<ApplyDTO>>> getByJob(@PathVariable Long jobopeningNo) {
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
            @RequestParam String status) {
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
