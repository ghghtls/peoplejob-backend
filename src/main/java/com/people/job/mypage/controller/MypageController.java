package com.people.job.mypage.controller;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.mypage.service.MypageService;
import com.people.job.resume.dto.ResumeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    // 개인회원 - 내 이력서
    @GetMapping("/resumes/{userNo}")
    public ResponseEntity<List<ResumeDTO>> myResumes(@PathVariable Long userNo) {
        return ResponseEntity.ok(mypageService.getMyResumes(userNo));
    }

    // 개인회원 - 내 지원내역
    @GetMapping("/applies/{userNo}")
    public ResponseEntity<List<ApplyDTO>> myApplies(@PathVariable Long userNo) {
        return ResponseEntity.ok(mypageService.getMyApplies(userNo));
    }

    // 기업회원 - 내 공고
    @GetMapping("/jobopenings/{companyNo}")
    public ResponseEntity<List<JobopeningDTO>> myJobs(@PathVariable Long companyNo) {
        return ResponseEntity.ok(mypageService.getMyJobopenings(companyNo));
    }

    // 기업회원 - 특정 공고에 대한 지원내역
    @GetMapping("/applies/job/{jobopeningNo}")
    public ResponseEntity<List<ApplyDTO>> appliesForJob(@PathVariable Long jobopeningNo) {
        return ResponseEntity.ok(mypageService.getAppliesForMyJob(jobopeningNo));
    }
}
