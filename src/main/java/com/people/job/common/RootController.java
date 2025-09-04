package com.people.job.common;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class RootController {
    @GetMapping("/")
    public void root(HttpServletResponse res) throws IOException {
        res.sendRedirect("/swagger-ui/index.html");
    }
}
