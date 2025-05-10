package com.people.job.api;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class TestUserController {

    @GetMapping("/{id}")
    public String getUserById(@PathVariable Long id) {
        return "User ID: " + id;
    }
}
