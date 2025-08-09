package com.people.job.user.service;

import com.people.job.user.dto.UserDTO;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.UserRepository;
import com.people.job.user.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Map<String, String> register(UserDTO dto) {
        log.info("회원가입 시도: {}", dto.getUserid());

        if (userRepository.existsByUserid(dto.getUserid())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // userType 설정: U(개인회원), C(기업회원)
        String userType = dto.getUserType() != null ? dto.getUserType() : "U";

        // role 설정: ROLE_USER, ROLE_COMPANY, ROLE_ADMIN
        String role = userType.equals("C") ? "ROLE_COMPANY" : "ROLE_USER";

        String emailCode = UUID.randomUUID().toString().substring(0, 8);

        UserEntity user = UserEntity.builder()
                .userid(dto.getUserid())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .zipcode(dto.getZipcode())
                .address(dto.getAddress())
                .addressDetail(dto.getAddressDetail())
                .userType(userType)
                .role(role)
                .emailVerified(false)  // 개발 중에는 true로 설정 가능
                .emailVerifyCode(emailCode)
                .build();

        userRepository.save(user);

        log.info("회원가입 성공 - ID: {}, 인증코드: {}", dto.getUserid(), emailCode);

        Map<String, String> result = new HashMap<>();
        result.put("message", "회원가입 성공");
        result.put("verifyCode", emailCode); // 개발용, 실제로는 이메일로 전송
        return result;
    }

    public Map<String, Object> login(String userid, String rawPassword) {
        log.info("로그인 시도: {}", userid);

        UserEntity user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 개발 환경에서는 이메일 인증 체크 비활성화 가능
        // if (!user.isEmailVerified()) {
        //     throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        // }

        String token = jwtTokenProvider.createToken(user.getUserid(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userid", user.getUserid());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("userType", user.getUserType());

        log.info("로그인 성공: {} - Role: {}", userid, user.getRole());

        return response;
    }

    public void verifyEmail(String userid, String code) {
        log.info("이메일 인증 시도: {}", userid);

        UserEntity user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!user.getEmailVerifyCode().equals(code)) {
            throw new RuntimeException("인증 코드가 일치하지 않습니다.");
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("이메일 인증 완료: {}", userid);
    }

    public UserEntity findByUserid(String userid) {
        return userRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}