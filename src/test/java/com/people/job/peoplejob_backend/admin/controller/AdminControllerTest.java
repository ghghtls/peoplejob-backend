package com.people.job.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.admin.dto.DashboardDTO;
import com.people.job.admin.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
@DisplayName("관리자 컨트롤러 테스트")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    private DashboardDTO testDashboard;

    @BeforeEach
    void setUp() {
        testDashboard = DashboardDTO.builder()
                .totalUsers(1000L)
                .totalJobs(500L)
                .totalApplications(2000L)
                .todayNewUsers(10L)
                .todayNewJobs(5L)
                .todayApplications(50L)
                .activeUsers(800L)
                .activeJobs(400L)
                .build();
    }

    @Test
    @DisplayName("대시보드 정보 조회 테스트")
    void getDashboard() throws Exception {
        // Given
        when(adminService.getDashboardData()).thenReturn(testDashboard);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(1000L))
                .andExpect(jsonPath("$.totalJobs").value(500L))
                .andExpect(jsonPath("$.totalApplications").value(2000L));
    }

    @Test
    @DisplayName("사용자 통계 조회 테스트")
    void getUserStats() throws Exception {
        // Given
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("total", 1000L);
        userStats.put("active", 800L);
        userStats.put("inactive", 200L);
        userStats.put("individual", 700L);
        userStats.put("company", 300L);

        when(adminService.getUserStatistics()).thenReturn(userStats);

        // When & Then
        mockMvc.perform(get("/api/admin/stats/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1000L))
                .andExpect(jsonPath("$.active").value(800L))
                .andExpect(jsonPath("$.individual").value(700L));
    }

    @Test
    @DisplayName("채용공고 통계 조회 테스트")
    void getJobStats() throws Exception {
        // Given
        Map<String, Object> jobStats = new HashMap<>();
        jobStats.put("total", 500L);
        jobStats.put("active", 400L);
        jobStats.put("expired", 100L);
        jobStats.put("thisMonth", 50L);

        when(adminService.getJobStatistics()).thenReturn(jobStats);

        // When & Then
        mockMvc.perform(get("/api/admin/stats/jobs"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(500L))
                .andExpect(jsonPath("$.active").value(400L))
                .andExpect(jsonPath("$.thisMonth").value(50L));
    }

    @Test
    @DisplayName("지원 통계 조회 테스트")
    void getApplicationStats() throws Exception{
        // Given
        Map<String, Object> applicationStats = new HashMap<>();
        applicationStats.put("total", 2000L);
        applicationStats.put("pending", 500L);
        applicationStats.put("accepted", 300L);
        applicationStats.put("rejected", 1200L);
        applicationStats.put("thisWeek", 150L);

        when(adminService.getApplicationStatistics()).thenReturn(applicationStats);

        // When & Then
        mockMvc.perform(get("/api/admin/stats/applications"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2000L))
                .andExpect(jsonPath("$.pending").value(500L))
                .andExpect(jsonPath("$.accepted").value(300L));
    }

    @Test
    @DisplayName("시스템 통계 조회 테스트")
    void getSystemStats() throws Exception {
        // Given
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("serverUptime", "7 days");
        systemStats.put("memoryUsage", "65%");
        systemStats.put("diskUsage", "45%");
        systemStats.put("activeConnections", 250L);

        when(adminService.getSystemStatistics()).thenReturn(systemStats);

        // When & Then
        mockMvc.perform(get("/api/admin/stats/system"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serverUptime").value("7 days"))
                .andExpect(jsonPath("$.memoryUsage").value("65%"));
    }

    @Test
    @DisplayName("사용자 관리 - 사용자 비활성화 테스트")
    void deactivateUser() throws Exception {
        // Given
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "사용자가 비활성화되었습니다.");

        when(adminService.deactivateUser(1L)).thenReturn(result);

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{userId}/deactivate", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자가 비활성화되었습니다."));
    }

    @Test
    @DisplayName("사용자 관리 - 사용자 활성화 테스트")
    void activateUser() throws Exception {
        // Given
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "사용자가 활성화되었습니다.");

        when(adminService.activateUser(1L)).thenReturn(result);

        // When & Then
        mockMvc.perform(patch("/api/admin/users/{userId}/activate", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자가 활성화되었습니다."));
    }

    @Test
    @DisplayName("채용공고 관리 - 공고 승인 테스트")
    void approveJob() throws Exception {
        // Given
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "채용공고가 승인되었습니다.");

        when(adminService.approveJob(1L)).thenReturn(result);

        // When & Then
        mockMvc.perform(patch("/api/admin/jobs/{jobId}/approve", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("채용공고가 승인되었습니다."));
    }

    @Test
    @DisplayName("채용공고 관리 - 공고 거부 테스트")
    void rejectJob() throws Exception {
        // Given
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "채용공고가 거부되었습니다.");

        when(adminService.rejectJob(eq(1L), any(String.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(patch("/api/admin/jobs/{jobId}/reject", 1L)
                        .param("reason", "부적절한 내용"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("채용공고가 거부되었습니다."));
    }

    @Test
    @DisplayName("기간별 통계 조회 테스트")
    void getStatsByPeriod() throws Exception {
        // Given
        Map<String, Object> periodStats = new HashMap<>();
        periodStats.put("period", "weekly");
        periodStats.put("newUsers", 70L);
        periodStats.put("newJobs", 35L);
        periodStats.put("newApplications", 350L);

        when(adminService.getStatsByPeriod(eq("weekly"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(periodStats);

        // When & Then
        mockMvc.perform(get("/api/admin/stats/period")
                        .param("period", "weekly")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-07"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("weekly"))
                .andExpect(jsonPath("$.newUsers").value(70L));
    }

    @Test
    @DisplayName("엑셀 보고서 생성 테스트")
    void generateExcelReport() throws Exception {
        // Given
        byte[] excelData = "Excel report content".getBytes();

        when(adminService.generateExcelReport(eq("users"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(excelData);

        // When & Then
        mockMvc.perform(get("/api/admin/reports/excel")
                        .param("type", "users")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=users_report.xlsx"));
    }

    @Test
    @DisplayName("시스템 설정 업데이트 테스트")
    void updateSystemSettings() throws Exception {
        // Given
        Map<String, Object> settings = new HashMap<>();
        settings.put("maintenanceMode", false);
        settings.put("registrationEnabled", true);
        settings.put("maxFileSize", "10MB");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "시스템 설정이 업데이트되었습니다.");

        when(adminService.updateSystemSettings(any())).thenReturn(result);

        // When & Then
        mockMvc.perform(put("/api/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settings)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("시스템 설정이 업데이트되었습니다."));
    }

    @Test
    @DisplayName("관리자 권한 없이 접근 시 403 에러 테스트")
    void accessWithoutAdminRole() throws Exception {
        // Given
        when(adminService.getDashboardData())
                .thenThrow(new SecurityException("관리자 권한이 필요합니다."));

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}