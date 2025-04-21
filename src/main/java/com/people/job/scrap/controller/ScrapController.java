package com.people.job.scrap.controller;

import com.people.job.scrap.dto.ScrapDTO;
import com.people.job.scrap.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrap")
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;

    // 스크랩 등록
    @PostMapping
    public ResponseEntity<?> add(@RequestBody ScrapDTO dto) {
        scrapService.addScrap(dto);
        return ResponseEntity.ok("스크랩 완료");
    }

    // 내 스크랩 목록
    @GetMapping("/{userNo}")
    public ResponseEntity<List<ScrapDTO>> myScraps(@PathVariable Long userNo) {
        return ResponseEntity.ok(scrapService.getScrapsByUser(userNo));
    }

    // 스크랩 개별 삭제
    @DeleteMapping("/{scrapNo}")
    public ResponseEntity<?> delete(@PathVariable Long scrapNo) {
        scrapService.deleteScrap(scrapNo);
        return ResponseEntity.ok("스크랩 삭제 완료");
    }

    // user + job 기준 삭제 (별표 토글형)
    @DeleteMapping
    public ResponseEntity<?> deleteByUserAndJob(@RequestParam Long userNo,
                                                @RequestParam Long jobopeningNo) {
        scrapService.deleteScrapByUserAndJob(userNo, jobopeningNo);
        return ResponseEntity.ok("스크랩 취소 완료");
    }
}
