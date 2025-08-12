package com.people.job.admin.controller;

import com.people.job.admin.dto.DashboardDTO;
import com.people.job.admin.service.AdminService;
import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.inquiry.service.InquiryService;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.payment.dto.PaymentDTO;
import com.people.job.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final InquiryService inquiryService;

    @GetMapping("/users")
    public ResponseEntity<List<UserEntity>> allUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @DeleteMapping("/users/{userNo}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userNo) {
        adminService.deleteUser(userNo);
        return ResponseEntity.ok("회원 삭제 완료");
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobopeningDTO>> allJobs() {
        return ResponseEntity.ok(adminService.getAllJobopenings());
    }

    @DeleteMapping("/jobs/{jobopeningNo}")
    public ResponseEntity<?> deleteJob(@PathVariable Long jobopeningNo) {
        adminService.deleteJobopening(jobopeningNo);
        return ResponseEntity.ok("공고 삭제 완료");
    }

    @GetMapping("/inquiries")
    public ResponseEntity<List<InquiryDTO>> allInquiries() {
        return ResponseEntity.ok(adminService.getAllInquiries());
    }

    @DeleteMapping("/inquiries/{inquiryNo}")
    public ResponseEntity<?> deleteInquiry(@PathVariable Long inquiryNo) {
        adminService.deleteInquiry(inquiryNo);
        return ResponseEntity.ok("문의 삭제 완료");
    }

    // 문의사항 답변 등록
    @PutMapping("/inquiries/{inquiryNo}/answer")
    public ResponseEntity<?> answerInquiry(@PathVariable Long inquiryNo, @RequestParam String answer, @RequestParam String answerBy) {
        adminService.answerInquiry(inquiryNo, answer, answerBy);
        return ResponseEntity.ok("답변 등록 완료");
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentDTO>> allPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> dashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
}