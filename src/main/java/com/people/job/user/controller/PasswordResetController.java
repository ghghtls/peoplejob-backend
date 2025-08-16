package com.people.job.user.controller;

import com.people.job.user.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
@Validated
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * 비밀번호 재설정 요청
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이메일이 필요합니다."
            ));
        }

        // 이메일 형식 검증
        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "올바른 이메일 형식이 아닙니다."
            ));
        }

        try {
            boolean success = passwordResetService.requestPasswordReset(email);

            if (success) {
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "비밀번호 재설정 이메일을 발송했습니다. 이메일을 확인해주세요."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "해당 이메일로 등록된 계정을 찾을 수 없습니다."
                ));
            }
        } catch (Exception e) {
            log.error("비밀번호 재설정 요청 처리 오류", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            ));
        }
    }

    /**
     * 토큰 유효성 검증
     */
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "토큰이 필요합니다."
            ));
        }

        try {
            boolean isValid = passwordResetService.validateResetToken(token);

            if (isValid) {
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "유효한 토큰입니다.",
                        "valid", true
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "유효하지 않거나 만료된 토큰입니다.",
                        "valid", false
                ));
            }
        } catch (Exception e) {
            log.error("토큰 검증 오류", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 비밀번호 재설정 실행
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        // 입력값 검증
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "토큰이 필요합니다."
            ));
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "새 비밀번호가 필요합니다."
            ));
        }

        if (newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "비밀번호는 8자 이상이어야 합니다."
            ));
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "비밀번호가 일치하지 않습니다."
            ));
        }

        try {
            boolean success = passwordResetService.resetPassword(token, newPassword);

            if (success) {
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "비밀번호가 성공적으로 재설정되었습니다."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "유효하지 않거나 만료된 토큰입니다."
                ));
            }
        } catch (Exception e) {
            log.error("비밀번호 재설정 실행 오류", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            ));
        }
    }

    /**
     * 간단한 이메일 유효성 검사
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}