package com.people.job.user.security;

import com.people.job.user.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // 공개 경로 화이트리스트 (스킵)
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/", "/index.html", "/favicon.ico", "/error",
            "/swagger-ui/**", "/v3/api-docs/**",
            "/actuator/health", "/actuator/info",
            "/api/users/login", "/api/users/register", "/api/users/verify", "/api/users/check/**",
            // 읽기 전용 공개 API
            "/api/job/**", "/api/board/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String pattern : PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(pattern, path)) return true;
        }
        return false;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 공개 경로 & 프리플라이트(OPTIONS)는 필터 스킵
        return isPublic(request) || HttpMethod.OPTIONS.matches(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Authorization 미존재 또는 Bearer 아님 -> 그냥 통과
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            if (jwtTokenProvider.validateToken(token)) {
                String userid = jwtTokenProvider.getUserid(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userid);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                filterChain.doFilter(request, response);
            } else {
                // 토큰이 있지만 유효하지 않으면 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception e) {
            // 사용자 조회 실패 등 예외도 401로 통일
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
