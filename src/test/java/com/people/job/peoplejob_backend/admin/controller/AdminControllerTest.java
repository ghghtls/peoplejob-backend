package com.people.job.peoplejob_backend.admin.controller;

import com.people.job.admin.controller.AdminController;
import com.people.job.admin.dto.DashboardDTO;
import com.people.job.admin.service.AdminService;
import com.people.job.admin.service.ExcelService;
import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.inquiry.service.InquiryService;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.payment.dto.PaymentDTO;
import com.people.job.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@ActiveProfiles("test")
@DisplayName("관리자 컨트롤러 테스트")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private InquiryService inquiryService;

    @MockitoBean
    private ExcelService excelService;

    private UserEntity testUser;
    private JobopeningDTO testJob;
    private InquiryDTO testInquiry;
    private PaymentDTO testPayment;
    private DashboardDTO testDashboard;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .userNo(1L)
                .userid("testuser")
                .username("테스트사용자")
                .email("test@example.com")
                .userType(UserEntity.UserType.INDIVIDUAL)
                .isActive(true)
                .build();

        testJob = JobopeningDTO.builder()
                .jobNo(1L)
                .title("백엔드 개발자 채용")
                .company("테스트 회사")
                .location("서울")
                .regdate(LocalDate.now())
                .build();

        testInquiry = InquiryDTO.builder()
                .inquiryNo(1L)
                .title("서비스 문의")
                .content("문의 내용입니다.")
                .writer("홍길동")
                .email("inquiry@example.com")
                .regdate(LocalDate.now())
                .build();

        testPayment = PaymentDTO.builder()
                .paymentNo(1L)
                .userNo(1L)
                .amount(java.math.BigDecimal.valueOf(29000))
                .paymentMethod("CARD")
                .paymentStatus("COMPLETED")
                .build();

        testDashboard = DashboardDTO.builder()
                .totalUsers(100L)
                .totalJobs(50L)
                .totalInquiries(20L)
                .totalPayments(30L)
                .build();
    }

    @Test
    @DisplayName("전체 회원 조회 성공 테스트")
    void getAllUsersSuccess() throws Exception {
        // Given
        List<UserEntity> users = Arrays.asList(testUser);
        when(adminService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/admin/users")) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userid").value("testuser"));
    }

    @Test
    @DisplayName("회원 삭제 성공 테스트")
    void deleteUserSuccess() throws Exception {
        // Given
        doNothing().when(adminService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/admin/users/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("회원 삭제 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("전체 채용공고 조회 성공 테스트")
    void getAllJobsSuccess() throws Exception {
        // Given
        List<JobopeningDTO> jobs = Arrays.asList(testJob);
        when(adminService.getAllJobopenings()).thenReturn(jobs);

        // When & Then
        mockMvc.perform(get("/api/admin/jobs"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("백엔드 개발자 채용"));
    }

    @Test
    @DisplayName("채용공고 삭제 성공 테스트")
    void deleteJobSuccess() throws Exception {
        // Given
        doNothing().when(adminService).deleteJobopening(1L);

        // When & Then
        mockMvc.perform(delete("/api/admin/jobs/{jobopeningNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("공고 삭제 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("전체 문의사항 조회 성공 테스트")
    void getAllInquiriesSuccess() throws Exception {
        // Given
        List<InquiryDTO> inquiries = Arrays.asList(testInquiry);
        when(adminService.getAllInquiries()).thenReturn(inquiries);

        // When & Then
        mockMvc.perform(get("/api/admin/inquiries"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("서비스 문의"));
    }

    @Test
    @DisplayName("문의사항 삭제 성공 테스트")
    void deleteInquirySuccess() throws Exception {
        // Given
        doNothing().when(adminService).deleteInquiry(1L);

        // When & Then
        mockMvc.perform(delete("/api/admin/inquiries/{inquiryNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("문의 삭제 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("문의사항 답변 등록 성공 테스트")
    void answerInquirySuccess() throws Exception {
        // Given
        doNothing().when(adminService).answerInquiry(eq(1L), anyString(), anyString());

        // When & Then
        mockMvc.perform(put("/api/admin/inquiries/{inquiryNo}/answer", 1L)
                        .param("answer", "답변 내용입니다.")
                        .param("answerBy", "관리자"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("답변 등록 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("전체 결제 내역 조회 성공 테스트")
    void getAllPaymentsSuccess() throws Exception {
        // Given
        List<PaymentDTO> payments = Arrays.asList(testPayment);
        when(adminService.getAllPayments()).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/admin/payments"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].paymentStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("대시보드 통계 조회 성공 테스트")
    void getDashboardSuccess() throws Exception {
        // Given
        when(adminService.getDashboardStats()).thenReturn(testDashboard);

        // When & Then
        mockMvc.perform(get("/api/admin/dashboard"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.totalJobs").value(50))
                .andExpect(jsonPath("$.totalInquiries").value(20))
                .andExpect(jsonPath("$.totalPayments").value(30));
    }

    @Test
    @DisplayName("회원 엑셀 다운로드 성공 테스트")
    void downloadUsersExcelSuccess() throws Exception {
        // Given
        byte[] excelData = "Excel data".getBytes();
        when(excelService.exportUsersToExcel()).thenReturn(org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=users.xlsx")
                .body(excelData));

        // When & Then
        mockMvc.perform(get("/api/admin/excel/users"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("채용공고 엑셀 다운로드 성공 테스트")
    void downloadJobsExcelSuccess() throws Exception {
        // Given
        byte[] excelData = "Excel data".getBytes();
        when(excelService.exportJobsToExcel()).thenReturn(org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=jobs.xlsx")
                .body(excelData));

        // When & Then
        mockMvc.perform(get("/api/admin/excel/jobs"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("지원자 엑셀 다운로드 성공 테스트")
    void downloadApplicantsExcelSuccess() throws Exception {
        // Given
        byte[] excelData = "Excel data".getBytes();
        when(excelService.exportApplicantsToExcel(1L)).thenReturn(org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=applicants_1.xlsx")
                .body(excelData));

        // When & Then
        mockMvc.perform(get("/api/admin/excel/applicants/{jobNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }
}