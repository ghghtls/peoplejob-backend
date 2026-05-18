package com.people.job.inquiry.controller;

import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 사용자 - 문의 등록
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody InquiryDTO dto) {
        inquiryService.insertInquiry(dto);
        return ResponseEntity.ok("문의가 등록되었습니다.");
    }



    // 사용자 - 내 문의 조회
    @GetMapping("/my")
    public ResponseEntity<List<InquiryDTO>> myInquiries(@RequestParam String email) {
        return ResponseEntity.ok(inquiryService.getInquiriesByEmail(email));
    }

    // 관리자 - 전체 문의 조회
    @GetMapping
    public ResponseEntity<List<InquiryDTO>> allInquiries() {
        return ResponseEntity.ok(inquiryService.getAllInquiries());
    }

    // 문의 상세 조회
    @GetMapping("/{inquiryNo}")
    public ResponseEntity<InquiryDTO> detail(@PathVariable Long inquiryNo) {
        return ResponseEntity.ok(inquiryService.getInquiry(inquiryNo));
    }

    // 문의 수정
    @PutMapping("/{inquiryNo}")
    public ResponseEntity<?> update(@PathVariable Long inquiryNo, @RequestBody InquiryDTO dto) {
        dto.setInquiryNo(inquiryNo);
        inquiryService.updateInquiry(dto);
        return ResponseEntity.ok("문의 수정 완료");
    }

    // 문의 삭제
    @DeleteMapping("/{inquiryNo}")
    public ResponseEntity<?> delete(@PathVariable Long inquiryNo) {
        inquiryService.deleteInquiry(inquiryNo);
        return ResponseEntity.ok("문의 삭제 완료");
    }

    // 관리자 - 답변 등록
    @PutMapping("/{inquiryNo}/answer")
    public ResponseEntity<?> answer(@PathVariable Long inquiryNo, @RequestParam String answer) {
        inquiryService.answerInquiry(inquiryNo, answer);
        return ResponseEntity.ok("답변 등록 완료");
    }
}
