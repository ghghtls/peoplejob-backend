package com.people.job.user.controller;

import com.people.job.user.dto.UserDTO;
import com.people.job.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}