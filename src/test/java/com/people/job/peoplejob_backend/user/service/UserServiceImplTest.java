package com.people.job.user.service;

import com.people.job.user.dto.UserDTO;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.UserRepository;
import com.people.job.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 서비스 구현체 테스트")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity testUserEntity;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUserEntity = UserEntity.builder()
                .userNo(1L)
                .userid("testuser")
                .password("encodedPassword")
                .name("테스트 사용자")
                .email("test@example.com")
                .phone("010-1234-5678")
                .userType("INDIVIDUAL")
                .role("USER")
                .isActive(true)
                .isEmailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        testUserDTO = UserDTO.builder()
                .userNo(1L)
                .userid("testuser")
                .password("password123")
                .name("테스트 사용자")
                .email("test@example.com")
                .phone("010-1234-5678")
                .userType("INDIVIDUAL")
                .role("USER")
                .isActive(true)
                .isEmailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerSuccess() {
        // Given
        when(userRepository.existsByUserid("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        // When
        Map<String, String> result = userService.register(testUserDTO);

        // Then
        assertNotNull(result);
        assertEquals("회원가입이 완료되었습니다.", result.get("message"));
        assertEquals("testuser", result.get("userid"));
        verify(userRepository).existsByUserid("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("중복 아이디로 회원가입 시 예외 발생 테스트")
    void registerWithDuplicateUserid() {
        // Given
        when(userRepository.existsByUserid("testuser")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.register(testUserDTO));
        verify(userRepository).existsByUserid("testuser");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 예외 발생 테스트")
    void registerWithDuplicateEmail() {
        // Given
        when(userRepository.existsByUserid("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.register(testUserDTO));
        verify(userRepository).existsByUserid("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        // Given
        when(userRepository.findByUserid("testuser")).thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser")).thenReturn("mock-jwt-token");

        // When
        Map<String, Object> result = userService.login("testuser", "password123");

        // Then
        assertNotNull(result);
        assertEquals("mock-jwt-token", result.get("token"));
        assertEquals("testuser", result.get("userid"));
        assertEquals("테스트 사용자", result.get("name"));
        assertEquals("INDIVIDUAL", result.get("userType"));
        verify(userRepository).findByUserid("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider).generateToken("testuser");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 시 예외 발생 테스트")
    void loginWithNonExistentUser() {
        // Given
        when(userRepository.findByUserid("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.login("nonexistent", "password"));
        verify(userRepository).findByUserid("nonexistent");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생 테스트")
    void loginWithWrongPassword() {
        // Given
        when(userRepository.findByUserid("testuser")).thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.login("testuser", "wrongpassword"));
        verify(userRepository).findByUserid("testuser");
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("비활성화된 사용자 로그인 시 예외 발생 테스트")
    void loginWithInactiveUser() {
        // Given
        UserEntity inactiveUser = UserEntity.builder()
                .userid("testuser")
                .password("encodedPassword")
                .isActive(false)
                .build();

        when(userRepository.findByUserid("testuser")).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // When & Then
        assertThrows(IllegalStateException.class, () -> userService.login("testuser", "password123"));
        verify(userRepository).findByUserid("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("사용자 정보 조회 테스트")
    void findByUserid() {
        // Given
        when(userRepository.findByUserid("testuser")).thenReturn(Optional.of(testUserEntity));

        // When
        UserDTO result = userService.findByUserid("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUserid());
        assertEquals("테스트 사용자", result.getName());
        verify(userRepository).findByUserid("testuser");
    }

    @Test
    @DisplayName("사용자 정보 수정 테스트")
    void updateUser() {
        // Given
        UserEntity updatedEntity = UserEntity.builder()
                .userNo(1L)
                .userid("testuser")
                .password("encodedPassword")
                .name("수정된 이름")
                .email("updated@example.com")
                .phone("010-9876-5432")
                .userType("INDIVIDUAL")
                .role("USER")
                .isActive(true)
                .isEmailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserDTO updateDTO = UserDTO.builder()
                .name("수정된 이름")
                .email("updated@example.com")
                .phone("010-9876-5432")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedEntity);

        // When
        UserDTO result = userService.updateUser(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("수정된 이름", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("비밀번호 변경 테스트")
    void changePassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        // When
        boolean result = userService.changePassword(1L, "currentPassword", "newPassword");

        // Then
        assertTrue(result);
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("currentPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("잘못된 현재 비밀번호로 비밀번호 변경 시 예외 발생 테스트")
    void changePasswordWithWrongCurrentPassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(1L, "wrongPassword", "newPassword"));
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode("newPassword");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("이메일 인증 테스트")
    void verifyEmail() {
        // Given
        when(userRepository.findByUserid("testuser")).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        // When
        userService.verifyEmail("testuser", "123456");

        // Then
        verify(userRepository).findByUserid("testuser");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("사용자 계정 비활성화 테스트")
    void deactivateUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        // When
        boolean result = userService.deactivateUser(1L);

        // Then
        assertTrue(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("사용자 계정 활성화 테스트")
    void activateUser() {
        // Given
        UserEntity inactiveUser = UserEntity.builder()
                .userNo(1L)
                .userid("testuser")
                .isActive(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);

        // When
        boolean result = userService.activateUser(1L);

        // Then
        assertTrue(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }
}