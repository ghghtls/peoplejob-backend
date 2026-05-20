package com.people.job.peoplejob_backend.apply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.apply.controller.ApplyController;
import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.repository.ApplyRepository;
import com.people.job.apply.service.ApplyService;
import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.UserRepository;
import com.people.job.user.security.JwtTokenProvider;
import com.people.job.user.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplyController.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("지원 컨트롤러 테스트")
class ApplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplyService applyService;

    @MockitoBean
    private ApplyRepository applyRepository;

    @MockitoBean
    private JobopeningRepository jobopeningRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private ApplyDTO testApply;
    private static final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        testApply = ApplyDTO.builder()
                .applyNo(1L)
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDate.now())
                .status("PENDING")
                .message("지원합니다.")
                .build();

        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUserid(anyString())).thenReturn("testuser");

        UserEntity testUser = UserEntity.builder()
                .userNo(1L)
                .userid("testuser")
                .username("테스트유저")
                .password("password")
                .email("test@test.com")
                .build();
        when(userRepository.findByUserid("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("지원하기 성공 테스트")
    void applySuccess() throws Exception {
        doNothing().when(applyService).applyToJob(any(ApplyDTO.class));

        mockMvc.perform(post("/api/apply")
                        .with(csrf())
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testApply)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원이 완료되었습니다."));
    }

    @Test
    @DisplayName("지원 실패 테스트 - 중복 지원")
    void applyFailDuplicate() throws Exception {
        doThrow(new RuntimeException("이미 지원한 공고입니다."))
                .when(applyService).applyToJob(any(ApplyDTO.class));

        mockMvc.perform(post("/api/apply")
                        .with(csrf())
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testApply)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("이력서별 지원 내역 조회 성공 테스트")
    void getAppliesByResumeSuccess() throws Exception {
        List<ApplyDTO> applies = Arrays.asList(testApply);
        when(applyService.getAppliesByResume(1L)).thenReturn(applies);

        mockMvc.perform(get("/api/apply/resume/{resumeNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].resumeNo").value(1));
    }

    @Test
    @DisplayName("채용공고별 지원자 목록 조회 성공 테스트")
    void getAppliesByJobSuccess() throws Exception {
        List<ApplyDTO> applicants = Arrays.asList(testApply);

        JobopeningEntity jobEntity = JobopeningEntity.builder()
                .jobNo(1L)
                .userNo(1L)
                .title("백엔드 개발자 채용")
                .isActive(true)
                .build();
        when(jobopeningRepository.findById(1L)).thenReturn(Optional.of(jobEntity));
        when(applyService.getAppliesByJobopening(1L)).thenReturn(applicants);

        mockMvc.perform(get("/api/apply/job/{jobopeningNo}", 1L)
                        .header("Authorization", AUTH_HEADER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].jobNo").value(1));
    }

    @Test
    @DisplayName("지원 취소 성공 테스트")
    void cancelApplySuccess() throws Exception {
        doNothing().when(applyService).cancelApply(1L);

        mockMvc.perform(delete("/api/apply/{applyNo}", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원이 취소되었습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 지원서 취소 테스트")
    void cancelApplyNotFound() throws Exception {
        doThrow(new RuntimeException("지원 내역을 찾을 수 없습니다."))
                .when(applyService).cancelApply(999L);

        mockMvc.perform(delete("/api/apply/{applyNo}", 999L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}
