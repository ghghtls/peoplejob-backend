package com.people.job.peoplejob_backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.user.controller.PasswordResetController;
import com.people.job.user.security.JwtTokenProvider;
import com.people.job.user.service.CustomUserDetailsService;
import com.people.job.user.service.PasswordResetService;
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

@WebMvcTest(PasswordResetController.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("비밀번호 재설정 컨트롤러 테스트")
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // ===== POST /api/password-reset/request =====

    @Test
    @DisplayName("비밀번호 재설정 요청 - 이메일 미입력")
    void requestReset_noEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/password-reset/request")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", ""))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 - 잘못된 이메일 형식")
    void requestReset_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/password-reset/request")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "notanemail"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("올바른 이메일 형식이 아닙니다."));
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 성공")
    void requestReset_success() throws Exception {
        when(passwordResetService.requestPasswordReset(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/password-reset/request")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "user@test.com"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 실패 - 등록되지 않은 이메일")
    void requestReset_notFound() throws Exception {
        when(passwordResetService.requestPasswordReset(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/password-reset/request")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "unknown@test.com"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 - 서버 예외")
    void requestReset_serviceException_returns500() throws Exception {
        when(passwordResetService.requestPasswordReset(anyString()))
                .thenThrow(new RuntimeException("서버 오류"));

        mockMvc.perform(post("/api/password-reset/request")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "user@test.com"))))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== GET /api/password-reset/validate-token =====

    @Test
    @DisplayName("토큰 검증 - 유효한 토큰")
    void validateToken_valid() throws Exception {
        when(passwordResetService.validateResetToken(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/password-reset/validate-token")
                        .param("token", "validtoken123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @DisplayName("토큰 검증 - 만료된 토큰")
    void validateToken_invalid() throws Exception {
        when(passwordResetService.validateResetToken(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/password-reset/validate-token")
                        .param("token", "expiredtoken"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("토큰 검증 - 빈 토큰")
    void validateToken_empty_returns400() throws Exception {
        mockMvc.perform(get("/api/password-reset/validate-token")
                        .param("token", "  "))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== POST /api/password-reset/reset =====

    @Test
    @DisplayName("비밀번호 재설정 - 토큰 미입력")
    void resetPassword_noToken_returns400() throws Exception {
        mockMvc.perform(post("/api/password-reset/reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "",
                                "newPassword", "NewPass123!",
                                "confirmPassword", "NewPass123!"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호 재설정 - 새 비밀번호 미입력")
    void resetPassword_noNewPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/password-reset/reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "validtoken",
                                "newPassword", "",
                                "confirmPassword", ""))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호 재설정 - 비밀번호 8자 미만")
    void resetPassword_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/password-reset/reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "validtoken",
                                "newPassword", "short",
                                "confirmPassword", "short"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호 재설정 - 비밀번호 불일치")
    void resetPassword_passwordMismatch_returns400() throws Exception {
        mockMvc.perform(post("/api/password-reset/reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "validtoken",
                                "newPassword", "NewPass123!",
                                "confirmPassword", "DifferentPass123!"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("비밀번호 재설정 성공")
    void resetPassword_success() throws Exception {
        when(passwordResetService.resetPassword(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/password-reset/reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "validtoken",
                                "newPassword", "NewPass123!",
                                "confirmPassword", "NewPass123!"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 재설정되었습니다."));
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 유효하지 않은 토큰")
    void resetPassword_invalidToken() throws Exception {
        when(passwordResetService.resetPassword(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/password-reset/reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "expiredtoken",
                                "newPassword", "NewPass123!",
                                "confirmPassword", "NewPass123!"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
