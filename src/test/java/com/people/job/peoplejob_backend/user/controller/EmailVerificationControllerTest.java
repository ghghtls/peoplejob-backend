package com.people.job.peoplejob_backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.user.controller.EmailVerificationController;
import com.people.job.user.security.JwtTokenProvider;
import com.people.job.user.service.CustomUserDetailsService;
import com.people.job.user.service.EmailVerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailVerificationController.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("이메일 인증 컨트롤러 테스트")
class EmailVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // ===== POST /api/email/send-verification =====

    @Test
    @DisplayName("이메일 인증코드 발송 - 이메일 미입력")
    void sendVerification_noEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/email/send-verification")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", ""))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("이메일 인증코드 발송 성공")
    void sendVerification_success() throws Exception {
        when(emailVerificationService.sendEmailVerification(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/email/send-verification")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "user@test.com"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인증코드가 발송되었습니다."));
    }

    @Test
    @DisplayName("이메일 인증코드 발송 실패")
    void sendVerification_failure() throws Exception {
        when(emailVerificationService.sendEmailVerification(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/email/send-verification")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "user@test.com"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== POST /api/email/verify =====

    @Test
    @DisplayName("이메일 인증 확인 - 인증코드 미입력")
    void verifyEmail_noCode_returns400() throws Exception {
        mockMvc.perform(post("/api/email/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", ""))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("이메일 인증 확인 성공")
    void verifyEmail_success() throws Exception {
        when(emailVerificationService.verifyEmail(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/email/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "123456"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이메일 인증이 완료되었습니다."));
    }

    @Test
    @DisplayName("이메일 인증 확인 실패 - 잘못된 코드")
    void verifyEmail_failure() throws Exception {
        when(emailVerificationService.verifyEmail(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/email/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "wrong"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== POST /api/email/send-reset-password =====

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 - 이메일 미입력")
    void sendPasswordReset_noEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/email/send-reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", ""))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 성공")
    void sendPasswordReset_success() throws Exception {
        when(emailVerificationService.sendPasswordResetEmail(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/email/send-reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "user@test.com"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 실패")
    void sendPasswordReset_failure() throws Exception {
        when(emailVerificationService.sendPasswordResetEmail(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/email/send-reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "user@test.com"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== POST /api/email/reset-password =====

    @Test
    @DisplayName("비밀번호 재설정 - 토큰 미입력")
    void resetPassword_noToken_returns400() throws Exception {
        mockMvc.perform(post("/api/email/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "", "newPassword", "Pass123!"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호 재설정 - 비밀번호 8자 미만")
    void resetPassword_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/email/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "validtoken", "newPassword", "short"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호 재설정 성공")
    void resetPassword_success() throws Exception {
        when(emailVerificationService.resetPassword(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/email/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "validtoken", "newPassword", "NewPass123!"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 만료된 토큰")
    void resetPassword_invalidToken() throws Exception {
        when(emailVerificationService.resetPassword(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/email/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "expiredtoken", "newPassword", "NewPass123!"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== GET /api/email/verify =====

    @Test
    @DisplayName("이메일 링크 인증 성공")
    void verifyEmailByLink_success() throws Exception {
        when(emailVerificationService.verifyEmail(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/email/verify")
                        .param("code", "validcode123"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 링크 인증 실패")
    void verifyEmailByLink_failure() throws Exception {
        when(emailVerificationService.verifyEmail(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/email/verify")
                        .param("code", "invalidcode"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
