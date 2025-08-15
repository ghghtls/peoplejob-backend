package com.people.job.user.controller;

import com.people.job.user.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증코드 발송
     */
    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "이메일을 입력해주세요."));
        }

        boolean success = emailVerificationService.sendEmailVerification(email);

        if (success) {
            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "인증코드가 발송되었습니다."));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "인증코드 발송에 실패했습니다."));
        }
    }

    /**
     * 이메일 인증 확인
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request) {
        String code = request.get("code");

        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "인증코드를 입력해주세요."));
        }

        boolean success = emailVerificationService.verifyEmail(code);

        if (success) {
            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "이메일 인증이 완료되었습니다."));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "유효하지 않거나 만료된 인증코드입니다."));
        }
    }

    /**
     * 비밀번호 재설정 이메일 발송
     */
    @PostMapping("/send-reset-password")
    public ResponseEntity<?> sendPasswordResetEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "이메일을 입력해주세요."));
        }

        boolean success = emailVerificationService.sendPasswordResetEmail(email);

        if (success) {
            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "비밀번호 재설정 링크가 발송되었습니다."));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "비밀번호 재설정 링크 발송에 실패했습니다."));
        }
    }

    /**
     * 비밀번호 재설정
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "재설정 토큰이 필요합니다."));
        }

        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "비밀번호는 8자 이상이어야 합니다."));
        }

        boolean success = emailVerificationService.resetPassword(token, newPassword);

        if (success) {
            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "유효하지 않거나 만료된 토큰입니다."));
        }
    }

    /**
     * GET 방식 이메일 인증 (이메일 링크 클릭 시)
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmailByLink(@RequestParam String code) {
        boolean success = emailVerificationService.verifyEmail(code);

        if (success) {
            return ResponseEntity.ok()
                    .body("<html><body><h2>✅ 이메일 인증이 완료되었습니다!</h2><p>이제 로그인하실 수 있습니다.</p></body></html>");
        } else {
            return ResponseEntity.badRequest()
                    .body("<html><body><h2>❌ 인증에 실패했습니다</h2><p>유효하지 않거나 만료된 인증코드입니다.</p></body></html>");
        }
    }
}