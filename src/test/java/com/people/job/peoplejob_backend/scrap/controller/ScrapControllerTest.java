package com.people.job.peoplejob_backend.scrap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.scrap.controller.ScrapController;
import com.people.job.scrap.dto.ScrapDTO;
import com.people.job.scrap.service.ScrapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
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

@WebMvcTest(ScrapController.class)
@ActiveProfiles("test")
@DisplayName("스크랩 컨트롤러 테스트")
class ScrapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScrapService scrapService;

    private ScrapDTO testScrap;

    @BeforeEach
    void setUp() {
        testScrap = ScrapDTO.builder()
                .scrapNo(1L)
                .userNo(1L)
                .jobNo(1L) // jobopeningNo -> jobNo (DB 스키마에 맞춤)
                .scrapDate(LocalDate.now())
                .jobTitle("백엔드 개발자 채용")
                .companyName("테스트 회사")
                .location("서울")
                .build();
    }

    @Test
    @DisplayName("스크랩 등록 성공 테스트")
    void addScrapSuccess() throws Exception {
        // Given
        doNothing().when(scrapService).addScrap(any(ScrapDTO.class)); // 실제 메서드명

        // When & Then
        mockMvc.perform(post("/api/scrap") // 실제 매핑 경로
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testScrap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("스크랩 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("내 스크랩 목록 조회 성공 테스트")
    void getMyScrapSuccess() throws Exception {
        // Given
        List<ScrapDTO> scraps = Arrays.asList(testScrap);
        when(scrapService.getScrapsByUser(1L)).thenReturn(scraps); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/scrap/{userNo}", 1L)) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].jobTitle").value("백엔드 개발자 채용"))
                .andExpect(jsonPath("$[0].userNo").value(1));
    }

    @Test
    @DisplayName("스크랩 개별 삭제 성공 테스트")
    void deleteScrapSuccess() throws Exception {
        // Given
        doNothing().when(scrapService).deleteScrap(1L); // 실제 메서드명

        // When & Then
        mockMvc.perform(delete("/api/scrap/{scrapNo}", 1L)) // 실제 매핑 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("스크랩 삭제 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("사용자+채용공고 기준 스크랩 삭제 성공 테스트")
    void deleteScrapByUserAndJobSuccess() throws Exception {
        // Given
        doNothing().when(scrapService).deleteScrapByUserAndJob(1L, 1L); // 실제 메서드명

        // When & Then
        mockMvc.perform(delete("/api/scrap") // 실제 매핑 경로
                        .param("userNo", "1")
                        .param("jobopeningNo", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("스크랩 취소 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("스크랩 등록 실패 테스트 - 중복 스크랩")
    void addScrapFailDuplicate() throws Exception {
        // Given
        doThrow(new RuntimeException("이미 스크랩한 공고입니다."))
                .when(scrapService).addScrap(any(ScrapDTO.class));

        // When & Then
        mockMvc.perform(post("/api/scrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testScrap)))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // RuntimeException으로 500 에러
    }

    @Test
    @DisplayName("내 스크랩 목록 조회 - 빈 목록 테스트")
    void getMyScrapEmpty() throws Exception {
        // Given
        List<ScrapDTO> emptyList = Arrays.asList();
        when(scrapService.getScrapsByUser(1L)).thenReturn(emptyList);

        // When & Then
        mockMvc.perform(get("/api/scrap/{userNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 스크랩 조회 테스트")
    void getMyScrapUserNotFound() throws Exception {
        // Given
        when(scrapService.getScrapsByUser(999L))
                .thenThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/scrap/{userNo}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 스크랩 삭제 테스트")
    void deleteScrapNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("스크랩을 찾을 수 없습니다."))
                .when(scrapService).deleteScrap(999L);

        // When & Then
        mockMvc.perform(delete("/api/scrap/{scrapNo}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 사용자+채용공고로 스크랩 삭제 테스트")
    void deleteScrapByUserAndJobNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("해당 스크랩을 찾을 수 없습니다."))
                .when(scrapService).deleteScrapByUserAndJob(999L, 999L);

        // When & Then
        mockMvc.perform(delete("/api/scrap")
                        .param("userNo", "999")
                        .param("jobopeningNo", "999"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}