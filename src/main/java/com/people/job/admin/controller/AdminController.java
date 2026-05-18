package com.people.job.admin.controller;

import com.people.job.admin.dto.DashboardDTO;
import com.people.job.admin.security.AdminRequired;
import com.people.job.admin.service.AdminService;
import com.people.job.admin.service.ExcelService;
import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.inquiry.service.InquiryService;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.payment.dto.PaymentDTO;
import com.people.job.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final InquiryService inquiryService;
    private final ExcelService excelService;

    @AdminRequired
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> allUsers() {
        List<Map<String, Object>> result = adminService.getAllUsers().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userNo", u.getUserNo());
            m.put("userid", u.getUserid());
            m.put("name", u.getUserRealName());
            m.put("email", u.getEmail());
            m.put("phone", u.getPhone());
            m.put("address", u.getAddress());
            m.put("userType", u.getUserType() != null ? u.getUserType().name() : null);
            m.put("isActive", u.getIsActive());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @AdminRequired
    @DeleteMapping("/users/{userNo}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userNo) {
        adminService.deleteUser(userNo);
        return ResponseEntity.ok("회원 삭제 완료");
    }

    @AdminRequired
    @GetMapping("/jobs")
    public ResponseEntity<List<JobopeningDTO>> allJobs() {
        return ResponseEntity.ok(adminService.getAllJobopenings());
    }

    @AdminRequired
    @DeleteMapping("/jobs/{jobopeningNo}")
    public ResponseEntity<?> deleteJob(@PathVariable Long jobopeningNo) {
        adminService.deleteJobopening(jobopeningNo);
        return ResponseEntity.ok("공고 삭제 완료");
    }

    @AdminRequired
    @GetMapping("/inquiries")
    public ResponseEntity<List<InquiryDTO>> allInquiries() {
        return ResponseEntity.ok(adminService.getAllInquiries());
    }

    @AdminRequired
    @DeleteMapping("/inquiries/{inquiryNo}")
    public ResponseEntity<?> deleteInquiry(@PathVariable Long inquiryNo) {
        adminService.deleteInquiry(inquiryNo);
        return ResponseEntity.ok("문의 삭제 완료");
    }

    @AdminRequired
    @PutMapping("/inquiries/{inquiryNo}/answer")
    public ResponseEntity<?> answerInquiry(@PathVariable Long inquiryNo, @RequestParam String answer, @RequestParam String answerBy) {
        adminService.answerInquiry(inquiryNo, answer, answerBy);
        return ResponseEntity.ok("답변 등록 완료");
    }

    @AdminRequired
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentDTO>> allPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    @AdminRequired
    @GetMapping("/applicants")
    public ResponseEntity<List<Map<String, Object>>> allApplicants() {
        return ResponseEntity.ok(adminService.getAllApplicants());
    }

    @AdminRequired
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> dashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ============ Excel 다운로드 API ============

    @AdminRequired
    @GetMapping("/excel/users")
    public ResponseEntity<byte[]> downloadUsersExcel() throws Exception {
        return excelService.exportUsersToExcel();
    }

    @AdminRequired
    @GetMapping("/excel/jobs")
    public ResponseEntity<byte[]> downloadJobsExcel() throws Exception {
        return excelService.exportJobsToExcel();
    }

    @AdminRequired
    @GetMapping("/excel/inquiries")
    public ResponseEntity<byte[]> downloadInquiriesExcel() throws Exception {
        return excelService.exportInquiriesToExcel();
    }

    @AdminRequired
    @GetMapping("/excel/applicants/{jobNo}")
    public ResponseEntity<byte[]> downloadApplicantsExcel(@PathVariable Long jobNo) throws Exception {
        return excelService.exportApplicantsToExcel(jobNo);
    }

    @AdminRequired
    @GetMapping("/excel/payments")
    public ResponseEntity<byte[]> downloadPaymentsExcel() throws Exception {
        return excelService.exportPaymentsToExcel();
    }

}