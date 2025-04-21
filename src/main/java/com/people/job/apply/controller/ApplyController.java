package com.people.job.apply.controller;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.service.ApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyService applyService;

    @PostMapping
    public ResponseEntity<?> apply(@RequestBody ApplyDTO dto) {
        applyService.applyToJob(dto);
        return ResponseEntity.ok("지원 완료!");
    }

    @GetMapping("/resume/{resumeNo}")
    public ResponseEntity<List<ApplyDTO>> getByResume(@PathVariable Long resumeNo) {
        return ResponseEntity.ok(applyService.getAppliesByResume(resumeNo));
    }

    @GetMapping("/job/{jobopeningNo}")
    public ResponseEntity<List<ApplyDTO>> getByJob(@PathVariable Long jobopeningNo) {
        return ResponseEntity.ok(applyService.getAppliesByJobopening(jobopeningNo));
    }

    @DeleteMapping("/{applyNo}")
    public ResponseEntity<?> cancel(@PathVariable Long applyNo) {
        applyService.cancelApply(applyNo);
        return ResponseEntity.ok("지원 취소 완료");
    }
}
