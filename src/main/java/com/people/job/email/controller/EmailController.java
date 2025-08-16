package com.people.job.email.controller;

import com.people.job.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * 이메일 발송 테스트
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTestEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String subject = request.get("subject");
        String content = request.get("content");

        if (to == null || to.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("받는 사람 이메일이 필요합니다.");
        }

        if (!emailService.isValidEmail(to)) {
            return ResponseEntity.badRequest().body("유효하지 않은 이메일 형식입니다.");
        }

        try {
            CompletableFuture<Boolean> result = emailService.sendSimpleEmail(
                    to,
                    subject != null ? subject : "테스트 이메일",
                    content != null ? content : "안녕하세요, 피플잡에서 보내는 테스트 이메일입니다."
            );

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "이메일 발송을 시작했습니다.",
                    "to", to
            ));
        } catch (Exception e) {
            log.error("이메일 발송 테스트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "이메일 발송에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 회원가입 인증 이메일 발송
     */
    @PostMapping("/verification")
    public ResponseEntity<?> sendVerificationEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String username = request.get("username");
        String verificationCode = request.get("verificationCode");

        if (to == null || username == null || verificationCode == null) {
            return ResponseEntity.badRequest().body("필수 정보가 누락되었습니다.");
        }

        if (!emailService.isValidEmail(to)) {
            return ResponseEntity.badRequest().body("유효하지 않은 이메일 형식입니다.");
        }

        try {
            CompletableFuture<Boolean> result = emailService.sendVerificationEmail(to, username, verificationCode);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "인증 이메일을 발송했습니다.",
                    "to", to
            ));
        } catch (Exception e) {
            log.error("인증 이메일 발송 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "인증 이메일 발송에 실패했습니다."
            ));
        }
    }

    /**
     * 비밀번호 재설정 이메일 발송
     */
    @PostMapping("/password-reset")
    public ResponseEntity<?> sendPasswordResetEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String username = request.get("username");
        String resetToken = request.get("resetToken");

        if (to == null || username == null || resetToken == null) {
            return ResponseEntity.badRequest().body("필수 정보가 누락되었습니다.");
        }

        if (!emailService.isValidEmail(to)) {
            return ResponseEntity.badRequest().body("유효하지 않은 이메일 형식입니다.");
        }

        try {
            CompletableFuture<Boolean> result = emailService.sendPasswordResetEmail(to, username, resetToken);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "비밀번호 재설정 이메일을 발송했습니다.",
                    "to", to
            ));
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "비밀번호 재설정 이메일 발송에 실패했습니다."
            ));
        }
    }

    /**
     * 이메일 유효성 검사
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateEmail(@RequestParam String email) {
        boolean isValid = emailService.isValidEmail(email);

        return ResponseEntity.ok().body(Map.of(
                "email", email,
                "isValid", isValid
        ));
    }
}