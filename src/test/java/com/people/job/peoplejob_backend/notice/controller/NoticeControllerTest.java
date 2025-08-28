package com.people.job.peoplejob_backend.notice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.notice.dto.NoticeDTO;
import com.people.job.notice.service.NoticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("공지사항 컨트롤러 테스트")
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoticeService noticeService;

    private NoticeDTO testNotice;

    @BeforeEach
    void setUp() {
        testNotice = NoticeDTO.builder()
                .noticeNo(1L)
                .title("시스템 점검 안내")
                .content("서버 점검으로 인한 서비스 일시 중단 안내입니다.")
                .writer("관리자") // author -> writer로 수정
                .regdate(LocalDate.now()) // 실제 필드명
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isImportant(true)
                .isActive(true) // isPublished -> isActive로 수정
                .viewCount(0)
                .build();
    }

    @Test
    @DisplayName("공지사항 전체 목록 조회 테스트")
    void getAllNotices() throws Exception {
        // Given
        List<NoticeDTO> noticeList = Arrays.asList(testNotice);
        when(noticeService.getAllActiveNotices()).thenReturn(noticeList);

        // When & Then
        mockMvc.perform(get("/api/notice")) // 실제 경로: /api/notice
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("시스템 점검 안내"));
    }

    @Test
    @DisplayName("공지사항 페이징 조회 테스트")
    void getNoticesWithPaging() throws Exception {
        // Given
        List<NoticeDTO> noticeList = Arrays.asList(testNotice);
        Page<NoticeDTO> noticePage = new PageImpl<>(noticeList, PageRequest.of(0, 10), 1);

        when(noticeService.getActiveNotices(any())).thenReturn(noticePage);

        // When & Then
        mockMvc.perform(get("/api/notice/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("시스템 점검 안내"));
    }

    @Test
    @DisplayName("공지사항 상세 조회 테스트")
    void getNoticeDetail() throws Exception {
        // Given
        when(noticeService.getNoticeDetail(1L)).thenReturn(testNotice);

        // When & Then
        mockMvc.perform(get("/api/notice/{noticeNo}", 1L)) // 실제 경로와 매개변수명
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("시스템 점검 안내"))
                .andExpect(jsonPath("$.content").value("서버 점검으로 인한 서비스 일시 중단 안내입니다."));
    }

    @Test
    @DisplayName("공지사항 등록 테스트")
    void createNotice() throws Exception {
        // Given
        NoticeDTO newNotice = NoticeDTO.builder()
                .title("새로운 기능 출시 안내")
                .content("새로운 기능이 출시되었습니다.")
                .writer("관리자")
                .isImportant(false)
                .build();

        when(noticeService.createNotice(any(NoticeDTO.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/notice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newNotice)))
                .andDo(print())
                .andExpect(status().isOk()) // 실제 응답은 200 OK
                .andExpect(jsonPath("$.message").value("공지사항이 등록되었습니다."))
                .andExpect(jsonPath("$.noticeId").value(1L));
    }

    @Test
    @DisplayName("공지사항 수정 테스트")
    void updateNotice() throws Exception {
        // Given
        NoticeDTO updatedNotice = NoticeDTO.builder()
                .noticeNo(1L)
                .title("시스템 점검 완료 안내")
                .content("서버 점검이 완료되었습니다.")
                .writer("관리자")
                .isImportant(true)
                .isActive(true)
                .viewCount(10)
                .build();

        // When & Then
        mockMvc.perform(put("/api/notice/{noticeNo}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedNotice)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항이 수정되었습니다."));
    }

    @Test
    @DisplayName("공지사항 삭제 테스트")
    void deleteNotice() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/notice/{noticeNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk()) // 실제 응답은 200 OK
                .andExpect(jsonPath("$.message").value("공지사항이 삭제되었습니다."));
    }

    @Test
    @DisplayName("중요 공지사항 조회 테스트")
    void getImportantNotices() throws Exception {
        // Given
        List<NoticeDTO> importantNotices = Arrays.asList(testNotice);
        when(noticeService.getImportantNotices()).thenReturn(importantNotices);

        // When & Then
        mockMvc.perform(get("/api/notice/important"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].isImportant").value(true));
    }

    @Test
    @DisplayName("공지사항 검색 테스트")
    void searchNotices() throws Exception {
        // Given
        List<NoticeDTO> searchResults = Arrays.asList(testNotice);
        Page<NoticeDTO> searchPage = new PageImpl<>(searchResults, PageRequest.of(0, 10), 1);

        when(noticeService.searchNotices(eq("점검"), any())).thenReturn(searchPage);

        // When & Then
        mockMvc.perform(get("/api/notice/search")
                        .param("keyword", "점검")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("시스템 점검 안내"));
    }

    @Test
    @DisplayName("최근 공지사항 조회 테스트")
    void getRecentNotices() throws Exception {
        // Given
        List<NoticeDTO> recentNotices = Arrays.asList(testNotice);
        when(noticeService.getRecentNotices(5)).thenReturn(recentNotices);

        // When & Then
        mockMvc.perform(get("/api/notice/recent")
                        .param("limit", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("시스템 점검 안내"));
    }

    @Test
    @DisplayName("공지사항 활성화/비활성화 상태 변경 테스트")
    void toggleNoticeStatus() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/notice/{noticeNo}/toggle-status", 1L)) // 실제 경로
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항 상태가 변경되었습니다."));
    }

    @Test
    @DisplayName("중요 공지 설정/해제 테스트")
    void toggleImportantStatus() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/notice/{noticeNo}/toggle-important", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항 중요도가 변경되었습니다."));
    }

    @Test
    @DisplayName("작성자별 공지사항 조회 테스트")
    void getNoticesByWriter() throws Exception {
        // Given
        List<NoticeDTO> noticesByWriter = Arrays.asList(testNotice);
        when(noticeService.getNoticesByWriter("관리자")).thenReturn(noticesByWriter);

        // When & Then
        mockMvc.perform(get("/api/notice/writer/{writer}", "관리자"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].writer").value("관리자"));
    }

    @Test
    @DisplayName("조회수 증가 테스트")
    void increaseViewCount() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/notice/{noticeNo}/view", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("조회수가 증가되었습니다."));
    }

    @Test
    @DisplayName("관리자용 모든 공지사항 조회 테스트")
    void getAllNoticesForAdmin() throws Exception {
        // Given
        List<NoticeDTO> allNotices = Arrays.asList(testNotice);
        Page<NoticeDTO> noticePage = new PageImpl<>(allNotices, PageRequest.of(0, 10), 1);

        when(noticeService.getAllNoticesForAdmin(any())).thenReturn(noticePage);

        // When & Then
        mockMvc.perform(get("/api/notice/admin/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("시스템 점검 안내"));
    }

    @Test
    @DisplayName("관리자용 공지사항 물리 삭제 테스트")
    void permanentDeleteNotice() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/notice/admin/{noticeNo}/permanent", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항이 완전히 삭제되었습니다."));
    }

    @Test
    @DisplayName("공지사항 통계 조회 테스트")
    void getNoticeStatistics() throws Exception {
        // Given
        List<NoticeDTO> allNotices = Arrays.asList(testNotice);
        List<NoticeDTO> importantNotices = Arrays.asList(testNotice);

        when(noticeService.getAllActiveNotices()).thenReturn(allNotices);
        when(noticeService.getImportantNotices()).thenReturn(importantNotices);

        // When & Then
        mockMvc.perform(get("/api/notice/admin/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNotices").value(1))
                .andExpect(jsonPath("$.importantNotices").value(1))
                .andExpect(jsonPath("$.totalViews").exists());
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 조회 시 오류 테스트")
    void getNoticeNotFound() throws Exception {
        // Given
        when(noticeService.getNoticeDetail(999L))
                .thenThrow(new RuntimeException("공지사항을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/notice/{noticeNo}", 999L))
                .andDo(print())
                .andExpect(status().isBadRequest()); // 실제 Controller에서는 badRequest() 반환
    }

    @Test
    @DisplayName("잘못된 데이터로 공지사항 등록 시 오류 테스트")
    void createNoticeWithInvalidData() throws Exception {
        // Given
        NoticeDTO invalidNotice = NoticeDTO.builder()
                .title("") // 빈 제목
                .content("내용")
                .writer("관리자")
                .build();

        when(noticeService.createNotice(any(NoticeDTO.class)))
                .thenThrow(new RuntimeException("제목은 필수입니다."));

        // When & Then
        mockMvc.perform(post("/api/notice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidNotice)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}