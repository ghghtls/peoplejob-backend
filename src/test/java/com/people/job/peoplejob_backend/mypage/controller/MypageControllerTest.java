package com.people.job.peoplejob_backend.mypage.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

public class MypageControllerTest {
}

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.mypage.service.MypageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("마이페이지 컨트롤러 테스트")
class MypageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MypageService mypageService;

    @Test
    @DisplayName("마이페이지 대시보드 조회 테스트")
    void getMypageDashboard() throws Exception {
        // Given
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalApplications", 15L);
        dashboard.put("pendingApplications", 5L);
        dashboard.put("acceptedApplications", 3L);
        dashboard.put("rejectedApplications", 7L);
        dashboard.put("totalScraps", 20L);
        dashboard.put("totalResumes", 2L);

        when(mypageService.getDashboardData(1L)).thenReturn(dashboard);

        // When & Then
        mockMvc.perform(get("/api/mypage/dashboard/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(15L))
                .andExpect(jsonPath("$.pendingApplications").value(5L))
                .andExpect(jsonPath("$.totalScraps").value(20L));
    }

    @Test
    @DisplayName("최근 활동 내역 조회 테스트")
    void getRecentActivities() throws Exception {
        // Given
        Map<String, Object> activities = new HashMap<>();
        activities.put("recentApplications", "최근 지원 내역");
        activities.put("recentScraps", "최근 스크랩 내역");
        activities.put("recentJobViews", "최근 조회한 채용공고");

        when(mypageService.getRecentActivities(1L)).thenReturn(activities);

        // When & Then
        mockMvc.perform(get("/api/mypage/activities/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recentApplications").exists())
                .andExpect(jsonPath("$.recentScraps").exists());
    }

    @Test
    @DisplayName("알림 설정 조회 테스트")
    void getNotificationSettings() throws Exception {
        // Given
        Map<String, Object> settings = new HashMap<>();
        settings.put("emailNotification", true);
        settings.put("smsNotification", false);
        settings.put("applicationStatusNotification", true);
        settings.put("jobRecommendationNotification", true);

        when(mypageService.getNotificationSettings(1L)).thenReturn(settings);

        // When & Then
        mockMvc.perform(get("/api/mypage/notifications/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailNotification").value(true))
                .andExpect(jsonPath("$.smsNotification").value(false));
    }

    @Test
    @DisplayName("알림 설정 업데이트 테스트")
    void updateNotificationSettings() throws Exception {
        // Given
        Map<String, Object> newSettings = new HashMap<>();
        newSettings.put("emailNotification", false);
        newSettings.put("smsNotification", true);
        newSettings.put("applicationStatusNotification", true);
        newSettings.put("jobRecommendationNotification", false);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "알림 설정이 업데이트되었습니다.");

        when(mypageService.updateNotificationSettings(eq(1L), any())).thenReturn(result);

        // When & Then
        mockMvc.perform(put("/api/mypage/notifications/{userNo}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newSettings)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("알림 설정이 업데이트되었습니다."));
    }
}