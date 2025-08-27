package com.people.job.user.controller;

import com.people.job.user.dto.UserDTO;
import com.people.job.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 개발용 CORS 설정
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO dto) {
        log.info("회원가입 요청 - ID: {}, Type: {}", dto.getUserid(), dto.getUserType());

        try {
            Map<String, String> result = userService.register(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("회원가입 실패: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO dto) {
        log.info("로그인 요청 - ID: {}", dto.getUserid());

        try {
            Map<String, Object> response = userService.login(dto.getUserid(), dto.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("로그인 실패: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(
            @RequestParam String userid,
            @RequestParam String code
    ) {
        log.info("이메일 인증 요청 - ID: {}", userid);

        try {
            userService.verifyEmail(userid, code);
            return ResponseEntity.ok(Map.of("message", "이메일 인증 완료!"));
        } catch (Exception e) {
            log.error("이메일 인증 실패: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check/{userid}")
    public ResponseEntity<?> checkUserid(@PathVariable String userid) {
        try {
            userService.findByUserid(userid);
            return ResponseEntity.ok(Map.of("available", false, "message", "이미 사용중인 아이디입니다."));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("available", true, "message", "사용 가능한 아이디입니다."));
        }
    }

    // ============ 회원 정보 관리 API ============

    // 회원 정보 조회
    @GetMapping("/profile/{userNo}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userNo) {
        log.info("회원 정보 조회 요청 - userNo: {}", userNo);

        try {
            UserDTO userProfile = userService.getUserProfile(userNo);
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            log.error("회원 정보 조회 실패: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 회원 정보 수정
    @PutMapping("/profile/{userNo}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long userNo,
            @RequestBody UserDTO dto
    ) {
        log.info("회원 정보 수정 요청 - userNo: {}, username: {}", userNo, dto.getUsername()); // name -> username으로 수정

        try {
            UserDTO updatedUser = userService.updateUserProfile(userNo, dto);
            return ResponseEntity.ok(Map.of(
                    "message", "회원 정보가 성공적으로 수정되었습니다.",
                    "user", updatedUser
            ));
        } catch (Exception e) {
            log.error("회원 정보 수정 실패: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 비밀번호 변경
    @PutMapping("/password/{userNo}")
    public ResponseEntity<?> changePassword(
            @PathVariable Long userNo,
            @RequestBody Map<String, String> passwordData
    ) {
        log.info("비밀번호 변경 요청 - userNo: {}", userNo);

        try {
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "현재 비밀번호와 새 비밀번호를 모두 입력해주세요."));
            }

            userService.changePassword(userNo, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (Exception e) {
            log.error("비밀번호 변경 실패: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 프로필 이미지 업로드
    @PostMapping("/profile/{userNo}/image")
    public ResponseEntity<?> uploadProfileImage(
            @PathVariable Long userNo,
            @RequestParam("file") MultipartFile file
    ) {
        log.info("프로필 이미지 업로드 요청 - userNo: {}, fileName: {}", userNo, file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "업로드할 파일을 선택해주세요."));
            }

            // 이미지 파일 검증
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일만 업로드 가능합니다."));
            }

            // 파일 크기 검증 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "파일 크기는 5MB를 초과할 수 없습니다."));
            }

            String imageUrl = userService.uploadProfileImage(userNo, file);
            return ResponseEntity.ok(Map.of(
                    "message", "프로필 이미지가 성공적으로 업로드되었습니다.",
                    "imageUrl", imageUrl
            ));
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 프로필 이미지 삭제
    @DeleteMapping("/profile/{userNo}/image")
    public ResponseEntity<?> deleteProfileImage(@PathVariable Long userNo) {
        log.info("프로필 이미지 삭제 요청 - userNo: {}", userNo);

        try {
            userService.deleteProfileImage(userNo);
            return ResponseEntity.ok(Map.of("message", "프로필 이미지가 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            log.error("프로필 이미지 삭제 실패: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/profile/{userNo}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userNo) {
        log.info("회원 탈퇴 요청 - userNo: {}", userNo);

        try {
            userService.deleteUser(userNo);
            return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 성공적으로 처리되었습니다."));
        } catch (Exception e) {
            log.error("회원 탈퇴 실패: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}