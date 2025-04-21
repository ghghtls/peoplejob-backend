package com.people.job.job.controller;

import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.service.JobopeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobopening")
@RequiredArgsConstructor
public class JobopeningController {

    private final JobopeningService jobopeningService;

    // 공고 등록
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody JobopeningDTO dto) {
        jobopeningService.insertJobopening(dto);
        return ResponseEntity.ok("공고 등록 성공");
    }

    // 전체 공고 조회
    @GetMapping
    public ResponseEntity<List<JobopeningDTO>> list() {
        List<JobopeningDTO> list = jobopeningService.selectAll();
        return ResponseEntity.ok(list);
    }

    // 개별 공고 조회
    @GetMapping("/{id}")
    public ResponseEntity<JobopeningDTO> detail(@PathVariable("id") Long jobopeningNo) {
        JobopeningDTO dto = jobopeningService.selectByNo(jobopeningNo);
        return ResponseEntity.ok(dto);
    }

    // 공고 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long jobopeningNo,
                                    @RequestBody JobopeningDTO dto) {
        dto.setJobopeningNo(jobopeningNo);
        jobopeningService.updateJobopening(dto);
        return ResponseEntity.ok("공고 수정 성공");
    }

    // 공고 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long jobopeningNo) {
        jobopeningService.deleteJobopening(jobopeningNo);
        return ResponseEntity.ok("공고 삭제 완료");
    }
}
