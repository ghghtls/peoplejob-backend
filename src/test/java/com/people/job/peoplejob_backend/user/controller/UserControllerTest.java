package com.people.job.peoplejob_backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.user.controller.UserController;
import com.people.job.user.dto.UserDTO;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.security.JwtTokenProvider;
import com.people.job.user.service.CustomUserDetailsService;
import com.people.job.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("사용자 컨트롤러 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UserDTO testUser;
    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        testUser = UserDTO.builder()
                .userid("testuser")
                .password("password123")
                .username("테스트 사용자") // name -> username으로 수정
                .email("test@example.com")
                .userType("INDIVIDUAL") // 정확한 Enum 값
                .regdate(LocalDate.now())
                .build();

        testUserEntity = UserEntity.builder()
                .userNo(1L)
                .userid("testuser")
                .username("테스트 사용자")
                .email("test@example.com")
                .userType(UserEntity.UserType.INDIVIDUAL)
                .regdate(LocalDate.now())
                .isActive(true)
                .isEmailVerified(true)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerSuccess() throws Exception {
        // Given
        Map<String, String> response = new HashMap<>();
        response.put("message", "회원가입이 완료되었습니다.");

        when(userService.register(any(UserDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복된 아이디")
    void registerFailDuplicateUserId() throws Exception {
        // Given
        when(userService.register(any(UserDTO.class)))
                .thenThrow(new RuntimeException("이미 사용중인 아이디입니다."));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미 사용중인 아이디입니다."));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() throws Exception {
        // Given
        Map<String, Object> loginResponse = new HashMap<>();
        loginResponse.put("message", "로그인 성공");
        loginResponse.put("user", testUser);

        when(userService.login(anyString(), anyString())).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void loginFailInvalidPassword() throws Exception {
        // Given
        when(userService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("비밀번호가 일치하지 않습니다."));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
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
                .thenThrow(new RuntimeException("존재하지 않는 사용자입니다."));

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
        when(userService.findByUserid(anyString())).thenReturn(testUserEntity);

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
        doNothing().when(userService).verifyEmail(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/users/verify")
                        .with(csrf())
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
        doThrow(new RuntimeException("인증 코드가 일치하지 않습니다."))
                .when(userService).verifyEmail(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/users/verify")
                        .with(csrf())
                        .param("userid", "testuser")
                        .param("code", "wrong-code"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("인증 코드가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("회원 정보 조회 성공 테스트")
    void getUserProfileSuccess() throws Exception {
        // Given
        when(userService.getUserProfile(1L)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userid").value("testuser"))
                .andExpect(jsonPath("$.username").value("테스트 사용자"));
    }

    @Test
    @DisplayName("회원 정보 수정 성공 테스트")
    void updateUserProfileSuccess() throws Exception {
        // Given
        UserDTO updatedUser = UserDTO.builder()
                .userNo(1L)
                .userid("testuser")
                .username("수정된 이름")
                .email("updated@example.com")
                .userType("INDIVIDUAL")
                .build();

        when(userService.updateUserProfile(eq(1L), any(UserDTO.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile/{userNo}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 정보가 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("비밀번호 변경 성공 테스트")
    void changePasswordSuccess() throws Exception {
        // Given
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "oldPassword");
        passwordData.put("newPassword", "newPassword123!");

        doNothing().when(userService).changePassword(eq(1L), anyString(), anyString());

        // When & Then
        mockMvc.perform(put("/api/users/password/{userNo}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 성공 테스트")
    void deleteUserSuccess() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/profile/{userNo}", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 성공적으로 처리되었습니다."));
    }

    @Test
    @DisplayName("프로필 이미지 업로드 - 빈 파일")
    void uploadProfileImage_emptyFile_returns400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/users/profile/{userNo}/image", 1L)
                        .file(emptyFile)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("업로드할 파일을 선택해주세요."));
    }

    @Test
    @DisplayName("프로필 이미지 업로드 - 이미지 아닌 파일")
    void uploadProfileImage_nonImageFile_returns400() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("file", "resume.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(multipart("/api/users/profile/{userNo}/image", 1L)
                        .file(pdfFile)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미지 파일만 업로드 가능합니다."));
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공")
    void uploadProfileImage_success_returns200() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("file", "profile.jpg", "image/jpeg", "fake-image".getBytes());

        when(userService.uploadProfileImage(eq(1L), any(MultipartFile.class)))
                .thenReturn("http://example.com/uploads/profile.jpg");

        mockMvc.perform(multipart("/api/users/profile/{userNo}/image", 1L)
                        .file(imageFile)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필 이미지가 성공적으로 업로드되었습니다."))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/uploads/profile.jpg"));
    }

    @Test
    @DisplayName("프로필 이미지 삭제 성공")
    void deleteProfileImage_success_returns200() throws Exception {
        doNothing().when(userService).deleteProfileImage(1L);

        mockMvc.perform(delete("/api/users/profile/{userNo}/image", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필 이미지가 성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("프로필 이미지 삭제 실패")
    void deleteProfileImage_failure_returns400() throws Exception {
        doThrow(new RuntimeException("이미지가 없습니다.")).when(userService).deleteProfileImage(1L);

        mockMvc.perform(delete("/api/users/profile/{userNo}/image", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미지가 없습니다."));
    }

    @Test
    @DisplayName("회원 정보 조회 실패")
    void getUserProfile_failure_returns400() throws Exception {
        when(userService.getUserProfile(999L))
                .thenThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/users/profile/{userNo}", 999L))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("회원 정보 수정 실패")
    void updateUserProfile_failure_returns400() throws Exception {
        when(userService.updateUserProfile(eq(1L), any(UserDTO.class)))
                .thenThrow(new RuntimeException("수정에 실패했습니다."));

        mockMvc.perform(put("/api/users/profile/{userNo}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("수정에 실패했습니다."));
    }

    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호 누락")
    void changePassword_missingCurrentPassword_returns400() throws Exception {
        java.util.Map<String, String> passwordData = new java.util.HashMap<>();
        passwordData.put("newPassword", "NewPass123!");
        // currentPassword 생략

        mockMvc.perform(put("/api/users/password/{userNo}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordData)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("현재 비밀번호와 새 비밀번호를 모두 입력해주세요."));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 서비스 예외")
    void changePassword_failure_returns400() throws Exception {
        java.util.Map<String, String> passwordData = new java.util.HashMap<>();
        passwordData.put("currentPassword", "WrongPass");
        passwordData.put("newPassword", "NewPass123!");

        doThrow(new RuntimeException("현재 비밀번호가 일치하지 않습니다."))
                .when(userService).changePassword(eq(1L), anyString(), anyString());

        mockMvc.perform(put("/api/users/password/{userNo}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordData)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("현재 비밀번호가 일치하지 않습니다."));
    }
}