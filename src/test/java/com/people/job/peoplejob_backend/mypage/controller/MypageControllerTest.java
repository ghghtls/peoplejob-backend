package com.people.job.peoplejob_backend.mypage.controller;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.mypage.controller.MypageController;
import com.people.job.mypage.service.MypageService;
import com.people.job.resume.dto.ResumeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MypageController.class)
@ActiveProfiles("test")
@DisplayName("마이페이지 컨트롤러 테스트")
class MypageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MypageService mypageService;

    private ResumeDTO testResume;
    private ApplyDTO testApply;
    private JobopeningDTO testJob;

    @BeforeEach
    void setUp() {
        testResume = ResumeDTO.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("백엔드 개발자 이력서")
                .content("상세한 이력서 내용")
                .education("컴퓨터공학과 학사")
                .career("Java/Spring 3년")
                .certificate("정보처리기사")
                .hopeJobtype("IT/소프트웨어")
                .hopeLocation("서울")
                .workType("정규직")
                .regdate(LocalDate.now())
                .build();

        testApply = ApplyDTO.builder()
                .applyNo(1L)
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDate.now())
                .status("PENDING")
                .message("지원합니다.")
                .jobTitle("백엔드 개발자")
                .companyName("테스트 회사")
                .build();

        testJob = JobopeningDTO.builder()
                .jobNo(1L)
                .title("백엔드 개발자 채용")
                .company("테스트 회사")
                .location("서울")
                .jobType("IT/소프트웨어")
                .userNo(1L)
                .regdate(LocalDate.now())
                .status("PUBLISHED")
                .build();
    }

    @Test
    @DisplayName("내 이력서 조회 성공 테스트")
    void getMyResumesSuccess() throws Exception {
        // Given
        List<ResumeDTO> resumes = Arrays.asList(testResume);
        when(mypageService.getMyResumes(1L)).thenReturn(resumes); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/mypage/resumes/{userNo}", 1L)) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("백엔드 개발자 이력서"))
                .andExpect(jsonPath("$[0].userNo").value(1));
    }

    @Test
    @DisplayName("내 지원내역 조회 성공 테스트")
    void getMyAppliesSuccess() throws Exception {
        // Given
        List<ApplyDTO> applies = Arrays.asList(testApply);
        when(mypageService.getMyApplies(1L)).thenReturn(applies); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/mypage/applies/{userNo}", 1L)) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].userNo").value(1));
    }

    @Test
    @DisplayName("내 채용공고 조회 성공 테스트 - 기업회원")
    void getMyJobsSuccess() throws Exception {
        // Given
        List<JobopeningDTO> jobs = Arrays.asList(testJob);
        when(mypageService.getMyJobopenings(1L)).thenReturn(jobs); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/mypage/jobopenings/{companyNo}", 1L)) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("백엔드 개발자 채용"))
                .andExpect(jsonPath("$[0].userNo").value(1));
    }

    @Test
    @DisplayName("특정 채용공고에 대한 지원내역 조회 성공 테스트")
    void getAppliesForJobSuccess() throws Exception {
        // Given
        List<ApplyDTO> applies = Arrays.asList(testApply);
        when(mypageService.getAppliesForMyJob(1L)).thenReturn(applies); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/mypage/applies/job/{jobopeningNo}", 1L)) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].jobNo").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("내 이력서 조회 - 빈 목록 테스트")
    void getMyResumesEmpty() throws Exception {
        // Given
        List<ResumeDTO> emptyList = Arrays.asList();
        when(mypageService.getMyResumes(1L)).thenReturn(emptyList);

        // When & Then
        mockMvc.perform(get("/api/mypage/resumes/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("내 지원내역 조회 - 빈 목록 테스트")
    void getMyAppliesEmpty() throws Exception {
        // Given
        List<ApplyDTO> emptyList = Arrays.asList();
        when(mypageService.getMyApplies(1L)).thenReturn(emptyList);

        // When & Then
        mockMvc.perform(get("/api/mypage/applies/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("내 채용공고 조회 - 빈 목록 테스트")
    void getMyJobsEmpty() throws Exception {
        // Given
        List<JobopeningDTO> emptyList = Arrays.asList();
        when(mypageService.getMyJobopenings(1L)).thenReturn(emptyList);

        // When & Then
        mockMvc.perform(get("/api/mypage/jobopenings/{companyNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 이력서 조회 테스트")
    void getMyResumesUserNotFound() throws Exception {
        // Given
        when(mypageService.getMyResumes(999L))
                .thenThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/mypage/resumes/{userNo}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // RuntimeException으로 500 에러
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 지원내역 조회 테스트")
    void getMyAppliesUserNotFound() throws Exception {
        // Given
        when(mypageService.getMyApplies(999L))
                .thenThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/mypage/applies/{userNo}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 기업의 채용공고 조회 테스트")
    void getMyJobsCompanyNotFound() throws Exception {
        // Given
        when(mypageService.getMyJobopenings(999L))
                .thenThrow(new RuntimeException("기업을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/mypage/jobopenings/{companyNo}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 채용공고에 대한 지원내역 조회 테스트")
    void getAppliesForJobNotFound() throws Exception {
        // Given
        when(mypageService.getAppliesForMyJob(999L))
                .thenThrow(new RuntimeException("채용공고를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/mypage/applies/job/{jobopeningNo}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}