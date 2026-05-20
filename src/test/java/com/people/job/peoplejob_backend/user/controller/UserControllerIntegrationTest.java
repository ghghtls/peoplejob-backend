package com.people.job.peoplejob_backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.user.dto.UserDTO;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser
@DisplayName("사용자 컨트롤러 통합 테스트")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDTO testUser;
    private UserEntity existingUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = UserDTO.builder()
                .userid("integrationtest")
                .password("TestPassword123!")
                .username("통합테스트 사용자") // name -> username
                .email("integration@test.com")
                .phone("010-1234-5678")
                .userType("INDIVIDUAL")
                .build();

        existingUser = UserEntity.builder()
                .userid("existinguser")
                .password(passwordEncoder.encode("TestPassword123!"))
                .username("기존 사용자") // name -> username
                .email("existing@test.com")
                .phone("010-9876-5432")
                .userType(UserEntity.UserType.INDIVIDUAL)
                .role(UserEntity.UserRole.USER) // String -> Enum
                .regdate(LocalDate.now())
                .isActive(true)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(existingUser);
    }

    @Test
    @DisplayName("실제 회원가입 플로우 테스트")
    void realRegisterFlow() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));

        // 실제로 데이터베이스에 저장되었는지 확인
        assert userRepository.findByUserid("integrationtest").isPresent();
    }

    @Test
    @DisplayName("실제 로그인 플로우 테스트")
    void realLoginFlow() throws Exception {
        // Given - 로그인용 DTO
        UserDTO loginUser = UserDTO.builder()
                .userid("existinguser")
                .password("TestPassword123!")
                .build();

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.user.userid").value("existinguser"))
                .andExpect(jsonPath("$.user.username").value("기존 사용자"));
    }

    @Test
    @DisplayName("중복 아이디 체크 실제 테스트")
    void realCheckDuplicateUserid() throws Exception {
        // 존재하는 아이디 체크
        mockMvc.perform(get("/api/users/check/{userid}", "existinguser"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용중인 아이디입니다."));

        // 사용 가능한 아이디 체크
        mockMvc.perform(get("/api/users/check/{userid}", "availableuser"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 아이디입니다."));
    }

    @Test
    @DisplayName("회원 정보 조회 실제 테스트")
    void realGetUserProfile() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/profile/{userNo}", existingUser.getUserNo()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userid").value("existinguser"))
                .andExpect(jsonPath("$.username").value("기존 사용자"))
                .andExpect(jsonPath("$.email").value("existing@test.com"));
    }

    @Test
    @DisplayName("사용자 정보 수정 실제 테스트")
    void realUpdateUser() throws Exception {
        // Given
        UserDTO updateUser = UserDTO.builder()
                .username("수정된 이름") // name -> username
                .email("updated@test.com")
                .phone("010-0000-0000")
                .address("수정된 주소")
                .build();

        // When & Then
        mockMvc.perform(put("/api/users/profile/{userNo}", existingUser.getUserNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 정보가 성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.user.username").value("수정된 이름"))
                .andExpect(jsonPath("$.user.email").value("updated@test.com"));

        // 실제로 데이터베이스에서 수정되었는지 확인
        UserEntity updated = userRepository.findById(existingUser.getUserNo()).orElse(null);
        assert updated != null;
        assert "수정된 이름".equals(updated.getUserRealName()); // getUserRealName() 사용
        assert "updated@test.com".equals(updated.getEmail());
    }

    @Test
    @DisplayName("잘못된 데이터로 회원가입 시 실제 검증 테스트")
    void realValidationTest() throws Exception {
        // Given - 잘못된 이메일 형식
        UserDTO invalidUser = UserDTO.builder()
                .userid("testuser")
                .password("short") // 너무 짧은 비밀번호
                .username("테스트")
                .email("invalid-email") // 잘못된 이메일
                .userType("INDIVIDUAL")
                .build();

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("비밀번호 변경 실제 테스트")
    void realChangePassword() throws Exception {
        // Given - Map 형태로 데이터 전송 (Controller에서 받는 형식에 맞춤)
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "TestPassword123!");
        passwordData.put("newPassword", "NewPassword123!");

        // When & Then
        mockMvc.perform(put("/api/users/password/{userNo}", existingUser.getUserNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));

        // 실제로 비밀번호가 변경되었는지 확인 (새 비밀번호로 로그인 시도)
        UserDTO loginTest = UserDTO.builder()
                .userid("existinguser")
                .password("NewPassword123!")
                .build();

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginTest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.user").exists());
    }

    @Test
    @DisplayName("이메일 인증 실제 테스트")
    void realVerifyEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/verify")
                        .param("userid", "existinguser")
                        .param("code", "123456"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증 완료!"));
    }

    @Test
    @DisplayName("회원 탈퇴 실제 테스트")
    void realDeleteUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/profile/{userNo}", existingUser.getUserNo()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 성공적으로 처리되었습니다."));

        // 실제로 비활성화되었는지 확인
        UserEntity deactivated = userRepository.findById(existingUser.getUserNo()).orElse(null);
        assert deactivated != null;
        assert !deactivated.getIsActive(); // 비활성화 확인
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 오류 테스트")
    void realGetUserProfileNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/profile/{userNo}", 999L))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("잘못된 현재 비밀번호로 변경 시도 테스트")
    void realChangePasswordWithWrongCurrentPassword() throws Exception {
        // Given
        Map<String, String> wrongPasswordData = new HashMap<>();
        wrongPasswordData.put("currentPassword", "WrongPassword123!");
        wrongPasswordData.put("newPassword", "NewPassword123!");

        // When & Then
        mockMvc.perform(put("/api/users/password/{userNo}", existingUser.getUserNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordData)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("현재 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시도 테스트")
    void realRegisterWithDuplicateEmail() throws Exception {
        // Given - 기존 사용자와 같은 이메일 사용
        UserDTO duplicateEmailUser = UserDTO.builder()
                .userid("newtestuser")
                .password("TestPassword123!")
                .username("새로운 사용자")
                .email("existing@test.com") // 기존 사용자 이메일
                .userType("INDIVIDUAL")
                .build();

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미 사용중인 이메일입니다."));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void realLoginFailWithWrongPassword() throws Exception {
        // Given
        UserDTO wrongLoginUser = UserDTO.builder()
                .userid("existinguser")
                .password("WrongPassword123!")
                .build();

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLoginUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 존재하지 않는 아이디")
    void realLoginFailWithNonExistentUser() throws Exception {
        // Given
        UserDTO nonExistentUser = UserDTO.builder()
                .userid("nonexistentuser")
                .password("TestPassword123!")
                .build();

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("존재하지 않는 아이디입니다."));
    }
}