package com.people.job.admin.security;

import com.people.job.user.entity.UserEntity;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminAuthAspect {

    @Before("@annotation(adminRequired)")
    public void checkAdminAuth(AdminRequired adminRequired) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserEntity)) {
            throw new RuntimeException("잘못된 사용자 정보입니다.");
        }

        UserEntity user = (UserEntity) principal;
        if (!"ROLE_ADMIN".equals(user.getRole())) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
    }
}