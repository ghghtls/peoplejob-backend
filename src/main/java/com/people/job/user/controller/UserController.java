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
        System.err.println("==================================");
        System.err.println("🔥 컨트롤러 호출됨!!!");
        System.err.println("🔥 userid: " + dto.getUserid());
        System.err.println("🔥 password: " + dto.getPassword());
        System.err.println("==================================");

        try {
            userService.register(dto);
            return ResponseEntity.ok("회원가입 성공! 이메일 인증 코드를 확인하세요.");
        } catch (Exception e) {
            System.err.println("🔥 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO dto) {
        String token = userService.login(dto.getUserid(), dto.getPassword());
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
