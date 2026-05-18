package com.people.job.mypage.controller;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.common.ApiResponse;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.mypage.service.MypageService;
import com.people.job.resume.dto.ResumeDTO;
import com.people.job.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    // ── 공통: 본인 또는 관리자만 접근 허용 ──────────────────────────────────
    private boolean isOwnerOrAdmin(UserEntity currentUser, Long targetUserNo) {
        return currentUser.getUserNo().equals(targetUserNo)
                || currentUser.getRole() == UserEntity.UserRole.ADMIN;
    }

    // 개인회원 - 내 이력서
    @GetMapping("/resumes/{userNo}")
    public ResponseEntity<ApiResponse<List<ResumeDTO>>> myResumes(
            @PathVariable Long userNo,
            @AuthenticationPrincipal UserEntity currentUser) {

        if (!isOwnerOrAdmin(currentUser, userNo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("접근 권한이 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(mypageService.getMyResumes(userNo)));
    }

    // 개인회원 - 내 지원내역
    @GetMapping("/applies/{userNo}")
    public ResponseEntity<ApiResponse<List<ApplyDTO>>> myApplies(
            @PathVariable Long userNo,
            @AuthenticationPrincipal UserEntity currentUser) {

        if (!isOwnerOrAdmin(currentUser, userNo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("접근 권한이 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(mypageService.getMyApplies(userNo)));
    }

    // 기업회원 - 내 공고 (본인 회사 또는 관리자)
    @GetMapping("/jobopenings/{companyNo}")
    public ResponseEntity<ApiResponse<List<JobopeningDTO>>> myJobs(
            @PathVariable Long companyNo,
            @AuthenticationPrincipal UserEntity currentUser) {

        if (!isOwnerOrAdmin(currentUser, companyNo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("접근 권한이 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(mypageService.getMyJobopenings(companyNo)));
    }

    // 기업회원 - 특정 공고에 대한 지원내역 (공고 소유자 확인은 서비스에서)
    @GetMapping("/applies/job/{jobopeningNo}")
    public ResponseEntity<ApiResponse<List<ApplyDTO>>> appliesForJob(
            @PathVariable Long jobopeningNo,
            @AuthenticationPrincipal UserEntity currentUser) {

        return ResponseEntity.ok(ApiResponse.success(mypageService.getAppliesForMyJob(jobopeningNo)));
    }
}
