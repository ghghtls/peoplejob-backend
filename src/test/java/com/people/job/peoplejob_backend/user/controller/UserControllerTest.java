package com.people.job.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.user.dto.UserDTO;
import com.people.job.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("사용자 컨트롤러 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO testUser;

    @BeforeEach
    void setUp() {
        testUser = UserDTO.builder()
                .userid("testuser")
                .password("password123")
                .name("테스트 사용자")
                .email("test@example.com")
                .userType("individual")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerSuccess() throws Exception {
        // Given
        Map<String, String> response = new HashMap<>();
        response.put("message", "회원가입이 완료되었습니다.");
        response.put("userid", testUser.getUserid());

        when(userService.register(any(UserDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.userid").value(testUser.getUserid()));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 잘못된 이메일 형식")
    void registerFailInvalidEmail() throws Exception {
        // Given
        testUser.setEmail("invalid-email");

        when(userService.register(any(UserDTO.class)))
                .thenThrow(new IllegalArgumentException("잘못된 이메일 형식입니다."));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("잘못된 이메일 형식입니다."));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() throws Exception {
        // Given
        Map<String, Object> loginResponse = new HashMap<>();
        loginResponse.put("token", "mock-jwt-token");
        loginResponse.put("userid", testUser.getUserid());
        loginResponse.put("name", testUser.getName());
        loginResponse.put("userType", testUser.getUserType());

        when(userService.login(anyString(), anyString())).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.userid").value(testUser.getUserid()));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void loginFailInvalidPassword() throws Exception {
        // Given
        when(userService.login(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다."));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("아이디 중복 체크 테스트 - 사용 가능")
    void checkUseridAvailable() throws Exception {
        // Given
        when(userService.findByUserid(anyString()))
                .thenThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/users/check/{userid}", "newuser"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 아이디입니다."));
    }

    @Test
    @DisplayName("아이디 중복 체크 테스트 - 이미 사용중")
    void checkUseridNotAvailable() throws Exception {
        // Given
        when(userService.findByUserid(anyString())).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/check/{userid}", "existinguser"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용중인 아이디입니다."));
    }

    @Test
    @DisplayName("이메일 인증 성공 테스트")
    void verifyEmailSuccess() throws Exception {
        // Given
        // UserService.verifyEmail은 void 메서드이므로 별도 설정 불필요

        // When & Then
        mockMvc.perform(post("/api/users/verify")
                        .param("userid", "testuser")
                        .param("code", "123456"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증 완료!"));
    }

    @Test
    @DisplayName("이메일 인증 실패 테스트 - 잘못된 코드")
    void verifyEmailFailInvalidCode() throws Exception {
        // Given
        when(userService.verifyEmail(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("인증 코드가 일치하지 않습니다."));

        // When & Then
        mockMvc.perform(post("/api/users/verify")
                        .param("userid", "testuser")
                        .param("code", "wrong-code"))
                .andDo(print())
                .andExpected(status().isBadRequest())
                .andExpected(jsonPath("$.error").value("인증 코드가 일치하지 않습니다."));
    }
}

