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

    // 메서드 무관 공개 경로
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/", "/index.html", "/favicon.ico", "/error",
            "/swagger-ui/**", "/v3/api-docs/**",
            "/actuator/health", "/actuator/info",
            "/api/users/login", "/api/users/register", "/api/users/verify", "/api/users/check/**",
            "/api/board/**"
    );

    // GET 전용 공개 경로 — 개별 job 상세(/api/jobs/{id})는 permit이지만
    // /api/jobs/user/** 처럼 인증이 필요한 하위 경로와 충돌하므로 ** 는 쓰지 않는다
    private static final List<String> GET_ONLY_PUBLIC_ENDPOINTS = List.of(
            "/api/jobs",
            "/api/jobs/search",
            "/api/jobs/category"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        for (String pattern : PUBLIC_ENDPOINTS) {
            if (PATH_MATCHER.match(pattern, path)) return true;
        }
        if (HttpMethod.GET.matches(method)) {
            for (String pattern : GET_ONLY_PUBLIC_ENDPOINTS) {
                if (PATH_MATCHER.match(pattern, path)) return true;
            }
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
            // 유효 토큰이면 인증 컨텍스트 세팅, 아니면 아무 것도 하지 않고 넘김
            if (jwtTokenProvider.validateToken(token)) {
                String userid = jwtTokenProvider.getUserid(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userid);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ignored) {
            // 여기서 응답 코드를 쓰지 않는다. (스프링 시큐리티가 적절히 처리)
        }

        // 항상 다음 필터로 넘긴다.
        filterChain.doFilter(request, response);
    }
}
