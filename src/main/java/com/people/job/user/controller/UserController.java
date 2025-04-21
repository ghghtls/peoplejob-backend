package com.people.job.user.controller;

import com.people.job.user.dto.UserDTO;
import com.people.job.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO dto) {
        userService.register(dto);
        return ResponseEntity.ok("회원가입 성공! 이메일 인증 코드를 확인하세요.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO dto) {
        String token = userService.login(dto.getUserid(), dto.getPwd());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(
            @RequestParam String userid,
            @RequestParam String code
    ) {
        userService.verifyEmail(userid, code);
        return ResponseEntity.ok("이메일 인증 완료!");
    }
}
