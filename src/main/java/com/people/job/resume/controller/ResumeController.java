package com.people.job.resume.controller;

import com.people.job.resume.dto.ResumeDTO;
import com.people.job.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<?> insert(@RequestBody ResumeDTO dto) {
        Long newResumeId = resumeService.insertResume(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "이력서 등록 완료");
        response.put("resumeId", newResumeId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ResumeDTO>> selectAll() {
        return ResponseEntity.ok(resumeService.selectAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDTO> selectByNo(@PathVariable("id") Long resumeNo) {
        return ResponseEntity.ok(resumeService.selectByNo(resumeNo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long resumeNo,
                                    @RequestBody ResumeDTO dto) {
        dto.setResumeNo(resumeNo);
        resumeService.updateResume(dto);
        return ResponseEntity.ok("이력서 수정 완료");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long resumeNo) {
        resumeService.deleteResume(resumeNo);
        return ResponseEntity.ok("이력서 삭제 완료");
    }

    @GetMapping("/user/{userNo}")
    public ResponseEntity<List<ResumeDTO>> selectByUser(@PathVariable Long userNo) {
        return ResponseEntity.ok(resumeService.selectByUserNo(userNo));
    }
}