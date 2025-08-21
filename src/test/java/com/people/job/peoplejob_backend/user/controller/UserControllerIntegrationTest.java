package com.people.job.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.user.dto.UserDTO;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
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
                .password("password123")
                .name("통합테스트 사용자")
                .email("integration@test.com")
                .phone("010-1234-5678")
                .userType("INDIVIDUAL")
                .build();

        existingUser = UserEntity.builder()
                .userid("existinguser")
                .password(passwordEncoder.encode("password123"))
                .name("기존 사용자")
                .email("existing@test.com")
                .phone("010-9876-5432")
                .userType("INDIVIDUAL")
                .role("USER")
                .isActive(true)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
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
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.userid").value("integrationtest"));

        // 실제로 데이터베이스에 저장되었는지 확인
        assert userRepository.findByUserid("integrationtest").isPresent();
    }

    @Test
    @DisplayName("실제 로그인 플로우 테스트")
    void realLoginFlow() throws Exception {
        // Given - 로그인용 DTO
        UserDTO loginUser = UserDTO.builder()
                .userid("existinguser")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userid").value("existinguser"))
                .andExpect(jsonPath("$.name").value("기존 사용자"));
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
    @DisplayName("사용자 정보 수정 실제 테스트")
    void realUpdateUser() throws Exception {
        // Given
        UserDTO updateUser = UserDTO.builder()
                .name("수정된 이름")
                .email("updated@test.com")
                .phone("010-0000-0000")
                .build();

        // When & Then
        mockMvc.perform(put("/api/users/{userNo}", existingUser.getUserNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된 이름"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));

        // 실제로 데이터베이스에서 수정되었는지 확인
        UserEntity updated = userRepository.findById(existingUser.getUserNo()).orElse(null);
        assert updated != null;
        assert "수정된 이름".equals(updated.getName());
        assert "updated@test.com".equals(updated.getEmail());
    }

    @Test
    @DisplayName("잘못된 데이터로 회원가입 시 실제 검증 테스트")
    void realValidationTest() throws Exception {
        // Given - 잘못된 이메일 형식
        UserDTO invalidUser = UserDTO.builder()
                .userid("testuser")
                .password("password123")
                .name("테스트")
                .email("invalid-email") // 잘못된 이메일
                .userType("INDIVIDUAL")
                .build();

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 변경 실제 테스트")
    void realChangePassword() throws Exception {
        // Given
        UserDTO.PasswordChangeRequest passwordChange = new UserDTO.PasswordChangeRequest();
        passwordChange.setCurrentPassword("password123");
        passwordChange.setNewPassword("newpassword123");
        passwordChange.setConfirmPassword("newpassword123");

        // When & Then
        mockMvc.perform(patch("/api/users/{userNo}/password", existingUser.getUserNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));

        // 실제로 비밀번호가 변경되었는지 확인 (새 비밀번호로 로그인 시도)
        UserDTO loginTest = UserDTO.builder()
                .userid("existinguser")
                .password("newpassword123")
                .build();

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginTest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}