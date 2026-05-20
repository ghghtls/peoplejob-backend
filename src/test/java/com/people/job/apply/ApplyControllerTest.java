package com.people.job.apply;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.apply.dto.ApplyDTO;
import com.people.job.user.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser
class ApplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void checkApply_nonExistentIds_returnsFalse() throws Exception {
        mockMvc.perform(get("/api/apply/check")
                        .param("jobNo", "99999")
                        .param("userNo", "99999"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void applyToJob_newApplication_returns200() throws Exception {
        ApplyDTO applyDto = ApplyDTO.builder()
                .jobNo(1L)
                .userNo(99999L)
                .resumeNo(System.currentTimeMillis())
                .message("CI 테스트 지원입니다.")
                .build();

        mockMvc.perform(post("/api/apply")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원이 완료되었습니다."));
    }

    @Test
    void applyFlow_apply_thenCheckApply_returnsTrue() throws Exception {
        long jobNo = 1L;
        long userNo = 88888L;
        long resumeNo = System.currentTimeMillis();

        ApplyDTO applyDto = ApplyDTO.builder()
                .jobNo(jobNo)
                .userNo(userNo)
                .resumeNo(resumeNo)
                .status("PENDING")
                .build();

        mockMvc.perform(post("/api/apply")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyDto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/apply/check")
                        .param("jobNo", String.valueOf(jobNo))
                        .param("userNo", String.valueOf(userNo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void applyDuplicate_secondApply_returns500() throws Exception {
        long jobNo = 2L;
        long resumeNo = System.currentTimeMillis();

        ApplyDTO first = ApplyDTO.builder()
                .jobNo(jobNo)
                .userNo(77777L)
                .resumeNo(resumeNo)
                .build();

        mockMvc.perform(post("/api/apply")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isOk());

        // 같은 jobNo + userNo로 재지원 → 이미 지원한 공고 예외 → 500
        mockMvc.perform(post("/api/apply")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}
