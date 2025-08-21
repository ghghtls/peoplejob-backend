package com.people.job.scrap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.scrap.dto.ScrapDTO;
import com.people.job.scrap.service.ScrapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("스크랩 컨트롤러 테스트")
class ScrapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScrapService scrapService;

    private ScrapDTO testScrap;

    @BeforeEach
    void setUp() {
        testScrap = ScrapDTO.builder()
                .scrapNo(1L)
                .userNo(1L)
                .jobNo(1L)
                .scrapDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("스크랩 목록 조회 테스트")
    void getScrapList() throws Exception {
        // Given
        List<ScrapDTO> scrapList = Arrays.asList(testScrap);
        Page<ScrapDTO> scrapPage = new PageImpl<>(scrapList, PageRequest.of(0, 10), 1);

        when(scrapService.findAll(any(), any())).thenReturn(scrapPage);

        // When & Then
        mockMvc.perform(get("/api/scraps")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userNo").value(1L));
    }

    @Test
    @DisplayName("스크랩 추가 테스트")
    void addScrap() throws Exception {
        // Given
        ScrapDTO newScrap = ScrapDTO.builder()
                .userNo(1L)
                .jobNo(2L)
                .build();

        when(scrapService.addScrap(any(ScrapDTO.class))).thenReturn(testScrap);

        // When & Then
        mockMvc.perform(post("/api/scraps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newScrap)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userNo").value(1L))
                .andExpect(jsonPath("$.jobNo").value(1L));
    }

    @Test
    @DisplayName("스크랩 삭제 테스트")
    void removeScrap() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/scraps/{id}", 1L))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("사용자별 스크랩 목록 조회 테스트")
    void getScrapsByUser() throws Exception {
        // Given
        List<ScrapDTO> userScraps = Arrays.asList(testScrap);
        Page<ScrapDTO> scrapPage = new PageImpl<>(userScraps, PageRequest.of(0, 10), 1);

        when(scrapService.findByUserNo(eq(1L), any())).thenReturn(scrapPage);

        // When & Then
        mockMvc.perform(get("/api/scraps/user/{userNo}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userNo").value(1L));
    }

    @Test
    @DisplayName("채용공고별 스크랩 개수 조회 테스트")
    void getScrapCountByJob() throws Exception {
        // Given
        when(scrapService.countByJobNo(1L)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/scraps/job/{jobNo}/count", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5L));
    }

    @Test
    @DisplayName("스크랩 여부 확인 테스트")
    void checkScrapStatus() throws Exception {
        // Given
        when(scrapService.isScraped(1L, 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/scraps/check")
                        .param("userNo", "1")
                        .param("jobNo", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isScraped").value(true));
    }

    @Test
    @DisplayName("중복 스크랩 시 400 에러 테스트")
    void duplicateScrap() throws Exception {
        // Given
        ScrapDTO duplicateScrap = ScrapDTO.builder()
                .userNo(1L)
                .jobNo(1L)
                .build();

        when(scrapService.addScrap(any(ScrapDTO.class)))
                .thenThrow(new IllegalArgumentException("이미 스크랩된 채용공고입니다."));

        // When & Then
        mockMvc.perform(post("/api/scraps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateScrap)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 스크랩 삭제 시 404 에러 테스트")
    void removeNonExistentScrap() throws Exception {
        // Given
        when(scrapService.removeScrap(999L))
                .thenThrow(new RuntimeException("스크랩을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(delete("/api/scraps/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}