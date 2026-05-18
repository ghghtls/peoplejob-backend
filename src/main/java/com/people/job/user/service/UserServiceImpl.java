package com.people.job.user.service;

import com.people.job.user.dto.UserDTO;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.UserRepository;
import com.people.job.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public Map<String, String> register(UserDTO dto) {
        // 기본 회원가입 로직 구현 필요
        try {
            // 아이디 중복 확인
            if (userRepository.findByUserid(dto.getUserid()).isPresent()) {
                throw new RuntimeException("이미 사용중인 아이디입니다.");
            }

            // 이메일 중복 확인
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("이미 사용중인 이메일입니다.");
            }

            // userType 변환: 프론트에서 "user"/"individual"/"company" (소문자) 가능
            String rawType = dto.getUserType() == null ? "INDIVIDUAL"
                    : dto.getUserType().trim().toUpperCase();
            if ("USER".equals(rawType)) rawType = "INDIVIDUAL";
            UserEntity.UserType userType;
            try {
                userType = UserEntity.UserType.valueOf(rawType);
            } catch (IllegalArgumentException e) {
                userType = UserEntity.UserType.INDIVIDUAL;
            }

            // userType에 따라 role 결정
            UserEntity.UserRole role = (userType == UserEntity.UserType.COMPANY)
                    ? UserEntity.UserRole.COMPANY
                    : UserEntity.UserRole.USER;

            // UserEntity 생성
            UserEntity user = UserEntity.builder()
                    .userid(dto.getUserid())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .username(dto.getUsername())
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .address(dto.getAddress())
                    .userType(userType)
                    .role(role)
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();

            userRepository.save(user);
            log.info("회원가입 완료 - userid: {}", dto.getUserid());

            return Map.of("message", "회원가입이 완료되었습니다.");
        } catch (Exception e) {
            log.error("회원가입 실패", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> login(String userid, String password) {
        try {
            UserEntity user = userRepository.findByUserid(userid)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }

            if (!user.getIsActive()) {
                throw new RuntimeException("비활성화된 계정입니다.");
            }

            String token = jwtTokenProvider.createToken(user.getUserid(), user.getRole().name());
            log.info("로그인 성공 - userid: {}", userid);

            // auth_service.dart 가 최상위에서 읽는 필드들 포함
            Map<String, Object> result = new HashMap<>();
            result.put("message", "로그인 성공");
            result.put("token", token);
            result.put("userid", user.getUserid());
            result.put("userNo", user.getUserNo());
            result.put("userType", user.getUserType().name());
            result.put("role", user.getRole().name());
            result.put("name", user.getUserRealName());
            result.put("email", user.getEmail());
            result.put("user", convertToDTO(user));
            return result;
        } catch (Exception e) {
            log.error("로그인 실패", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void verifyEmail(String userid, String code) {
        UserEntity user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 이메일 인증 로직 구현 필요
        user.setIsEmailVerified(true);
        userRepository.save(user);

        log.info("이메일 인증 완료 - userid: {}", userid);
    }

    @Override
    public UserEntity findByUserid(String userid) {
        return userRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserProfile(Long userNo) {
        UserEntity user = userRepository.findById(userNo)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return convertToDTO(user);
    }

    @Override
    public UserDTO updateUserProfile(Long userNo, UserDTO dto) {
        UserEntity user = userRepository.findById(userNo)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 기본 정보 업데이트 - name -> username으로 수정
        user.updateBasicInfo(
                dto.getUsername(), // name -> username으로 수정
                dto.getEmail(),
                dto.getPhone(),
                dto.getAddress(),
                dto.getDetailAddress(),
                dto.getZipcode()
        );

        // 기업회원인 경우 기업 정보도 업데이트
        if (user.getUserType() == UserEntity.UserType.COMPANY) {
            user.updateCompanyInfo(
                    dto.getCompanyName(),
                    dto.getBusinessNumber(),
                    dto.getCompanyPhone(),
                    dto.getCompanyAddress(),
                    dto.getCeoName(),
                    dto.getCompanyType(),
                    dto.getEmployeeCount(),
                    dto.getEstablishedYear(),
                    dto.getWebsite(),
                    dto.getCompanyDescription()
            );
        }

        UserEntity savedUser = userRepository.save(user);
        log.info("회원 정보 수정 완료 - userNo: {}", userNo);

        return convertToDTO(savedUser);
    }

    @Override
    public void changePassword(Long userNo, String currentPassword, String newPassword) {
        UserEntity user = userRepository.findById(userNo)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검증
        validatePassword(newPassword);

        // 새 비밀번호로 변경
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("비밀번호 변경 완료 - userNo: {}", userNo);
    }

    @Override
    public String uploadProfileImage(Long userNo, MultipartFile file) {
        UserEntity user = userRepository.findById(userNo)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        try {
            // 기존 프로필 이미지 삭제
            if (user.hasProfileImage()) {
                deleteExistingProfileImage(user.getProfileImageStoredName());
            }

            // 파일 저장
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String storedFilename = generateStoredFilename(userNo, fileExtension);

            // 업로드 디렉토리 생성
            File uploadDir = new File(uploadPath + "/profiles");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 파일 저장
            Path filePath = Paths.get(uploadDir.getAbsolutePath(), storedFilename);
            Files.write(filePath, file.getBytes());

            // URL 생성
            String imageUrl = baseUrl + "/uploads/profiles/" + storedFilename;

            // 사용자 엔티티에 이미지 정보 저장
            user.updateProfileImage(imageUrl, originalFilename, storedFilename);
            userRepository.save(user);

            log.info("프로필 이미지 업로드 완료 - userNo: {}, filename: {}", userNo, storedFilename);

            return imageUrl;

        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패 - userNo: {}", userNo, e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void deleteProfileImage(Long userNo) {
        UserEntity user = userRepository.findById(userNo)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.hasProfileImage()) {
            // 물리적 파일 삭제
            deleteExistingProfileImage(user.getProfileImageStoredName());

            // DB에서 이미지 정보 삭제
            user.clearProfileImage();
            userRepository.save(user);

            log.info("프로필 이미지 삭제 완료 - userNo: {}", userNo);
        }
    }

    @Override
    public void deleteUser(Long userNo) {
        UserEntity user = userRepository.findById(userNo)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 프로필 이미지 삭제
        if (user.hasProfileImage()) {
            deleteExistingProfileImage(user.getProfileImageStoredName());
        }

        // 사용자 비활성화 (실제 삭제 대신)
        user.setIsActive(false);
        userRepository.save(user);

        log.info("회원 탈퇴 처리 완료 - userNo: {}", userNo);
    }

    // ============ 헬퍼 메서드들 ============

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("비밀번호는 최소 8자 이상이어야 합니다.");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("비밀번호는 대문자를 포함해야 합니다.");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("비밀번로는 소문자를 포함해야 합니다.");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new RuntimeException("비밀번호는 숫자를 포함해야 합니다.");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new RuntimeException("비밀번호는 특수문자를 포함해야 합니다.");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String generateStoredFilename(Long userNo, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("profile_%d_%s_%s%s", userNo, timestamp, uuid, extension);
    }

    private void deleteExistingProfileImage(String storedFilename) {
        if (storedFilename != null && !storedFilename.trim().isEmpty()) {
            try {
                Path filePath = Paths.get(uploadPath, "profiles", storedFilename);
                Files.deleteIfExists(filePath);
                log.info("기존 프로필 이미지 삭제 완료: {}", storedFilename);
            } catch (IOException e) {
                log.warn("기존 프로필 이미지 삭제 실패: {}", storedFilename, e);
            }
        }
    }

    private UserDTO convertToDTO(UserEntity user) {
        return UserDTO.builder()
                .userNo(user.getUserNo())
                .userid(user.getUserid())
                .username(user.getUserRealName()) // getUserRealName() 메서드 사용
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .detailAddress(user.getDetailAddress())
                .zipcode(user.getZipcode())
                .userType(user.getUserType().name())
                .role(user.getRole().name())
                .regdate(user.getRegdate()) // LocalDate 타입
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .profileImageFilename(user.getProfileImageFilename())
                .companyName(user.getCompanyName())
                .businessNumber(user.getBusinessNumber())
                .companyPhone(user.getCompanyPhone())
                .companyAddress(user.getCompanyAddress())
                .ceoName(user.getCeoName())
                .companyType(user.getCompanyType())
                .employeeCount(user.getEmployeeCount())
                .establishedYear(user.getEstablishedYear())
                .website(user.getWebsite())
                .companyDescription(user.getCompanyDescription())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}