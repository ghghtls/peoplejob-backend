package com.people.job.peoplejob_backend.mypage.controller;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.mypage.controller.MypageController;
import com.people.job.mypage.service.MypageService;
import com.people.job.resume.dto.ResumeDTO;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.security.JwtTokenProvider;
import com.people.job.user.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MypageController.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("마이페이지 컨트롤러 테스트")
class MypageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MypageService mypageService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private ResumeDTO testResume;
    private ApplyDTO testApply;
    private JobopeningDTO testJob;
    private Authentication adminAuth;

    @BeforeEach
    void setUp() {
        UserEntity adminUser = UserEntity.builder()
                .userNo(1L)
                .userid("admin")
                .username("관리자")
                .role(UserEntity.UserRole.ADMIN)
                .build();
        adminAuth = new UsernamePasswordAuthenticationToken(
                adminUser, null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));

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
        List<ResumeDTO> resumes = Arrays.asList(testResume);
        when(mypageService.getMyResumes(1L)).thenReturn(resumes);

        mockMvc.perform(get("/api/mypage/resumes/{userNo}", 1L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("백엔드 개발자 이력서"))
                .andExpect(jsonPath("$.data[0].userNo").value(1));
    }

    @Test
    @DisplayName("내 지원내역 조회 성공 테스트")
    void getMyAppliesSuccess() throws Exception {
        List<ApplyDTO> applies = Arrays.asList(testApply);
        when(mypageService.getMyApplies(1L)).thenReturn(applies);

        mockMvc.perform(get("/api/mypage/applies/{userNo}", 1L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data[0].userNo").value(1));
    }

    @Test
    @DisplayName("내 채용공고 조회 성공 테스트 - 기업회원")
    void getMyJobsSuccess() throws Exception {
        List<JobopeningDTO> jobs = Arrays.asList(testJob);
        when(mypageService.getMyJobopenings(1L)).thenReturn(jobs);

        mockMvc.perform(get("/api/mypage/jobopenings/{companyNo}", 1L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("백엔드 개발자 채용"))
                .andExpect(jsonPath("$.data[0].userNo").value(1));
    }

    @Test
    @DisplayName("특정 채용공고에 대한 지원내역 조회 성공 테스트")
    void getAppliesForJobSuccess() throws Exception {
        List<ApplyDTO> applies = Arrays.asList(testApply);
        when(mypageService.getAppliesForMyJob(1L)).thenReturn(applies);

        mockMvc.perform(get("/api/mypage/applies/job/{jobopeningNo}", 1L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].jobNo").value(1))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("내 이력서 조회 - 빈 목록 테스트")
    void getMyResumesEmpty() throws Exception {
        when(mypageService.getMyResumes(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mypage/resumes/{userNo}", 1L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("내 지원내역 조회 - 빈 목록 테스트")
    void getMyAppliesEmpty() throws Exception {
        when(mypageService.getMyApplies(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mypage/applies/{userNo}", 1L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("내 채용공고 조회 - 빈 목록 테스트")
    void getMyJobsEmpty() throws Exception {
        when(mypageService.getMyJobopenings(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/mypage/jobopenings/{companyNo}", 1L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 이력서 조회 테스트")
    void getMyResumesUserNotFound() throws Exception {
        when(mypageService.getMyResumes(999L))
                .thenThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/mypage/resumes/{userNo}", 999L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 지원내역 조회 테스트")
    void getMyAppliesUserNotFound() throws Exception {
        when(mypageService.getMyApplies(999L))
                .thenThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/mypage/applies/{userNo}", 999L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 기업의 채용공고 조회 테스트")
    void getMyJobsCompanyNotFound() throws Exception {
        when(mypageService.getMyJobopenings(999L))
                .thenThrow(new RuntimeException("기업을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/mypage/jobopenings/{companyNo}", 999L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 채용공고에 대한 지원내역 조회 테스트")
    void getAppliesForJobNotFound() throws Exception {
        when(mypageService.getAppliesForMyJob(999L))
                .thenThrow(new RuntimeException("채용공고를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/mypage/applies/job/{jobopeningNo}", 999L)
                        .with(authentication(adminAuth)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}
