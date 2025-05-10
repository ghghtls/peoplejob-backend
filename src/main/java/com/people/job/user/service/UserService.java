package com.people.job.user.service;

import com.people.job.user.domain.User;
import com.people.job.user.dto.UserDTO;
import com.people.job.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(UserDTO dto) {
        if (userRepository.existsByUserid(dto.getUserid())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        String emailCode = UUID.randomUUID().toString().substring(0, 8);

        User user = User.builder()
                .userid(dto.getUserid())
                .password(passwordEncoder.encode(dto.getPwd()))
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .zipcode(dto.getZipcode())
                .address(dto.getAddress())
                .addressDetail(dto.getAddressDetail())
                .userType(dto.getUserType())
                .role("user")
                .emailVerified(false)
                .emailVerifyCode(emailCode)
                .build();

        userRepository.save(user);

        // TODO: 이메일 인증 코드 발송 로직 (SMTP 연동 또는 외부 서비스)
        System.out.println("인증 코드: " + emailCode);
    }

    public String login(String userid, String rawPassword) {
        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        if (!user.isEmailVerified()) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        // TODO: JWT 토큰 발급
        return "TOKEN_MOCK"; // 나중에 jwtTokenProvider.createToken(...)으로 변경
    }

    public void verifyEmail(String userid, String code) {
        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!user.getEmailVerifyCode().equals(code)) {
            throw new RuntimeException("인증 코드가 일치하지 않습니다.");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
    }
}
