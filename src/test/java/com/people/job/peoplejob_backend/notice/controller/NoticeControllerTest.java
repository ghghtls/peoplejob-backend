package com.people.job.peoplejob_backend.notice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.notice.controller.NoticeController;
import com.people.job.notice.dto.NoticeDTO;
import com.people.job.notice.service.NoticeService;
import com.people.job.user.security.JwtTokenProvider;
import com.people.job.user.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoticeController.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("공지사항 컨트롤러 테스트")
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoticeService noticeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private NoticeDTO testNotice;

    @BeforeEach
    void setUp() {
        testNotice = NoticeDTO.builder()
                .noticeNo(1L)
                .title("시스템 점검 안내")
                .content("서버 점검으로 인한 서비스 일시 중단 안내입니다.")
                .writer("관리자")
                .regdate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isImportant(true)
                .isActive(true)
                .viewCount(0)
                .build();
    }

    @Test
    @DisplayName("공지사항 전체 목록 조회 테스트")
    void getAllNotices() throws Exception {
        List<NoticeDTO> noticeList = Arrays.asList(testNotice);
        when(noticeService.getAllActiveNotices()).thenReturn(noticeList);

        mockMvc.perform(get("/api/notice"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("시스템 점검 안내"));
    }

    @Test
    @DisplayName("공지사항 페이징 조회 테스트")
    void getNoticesWithPaging() throws Exception {
        List<NoticeDTO> noticeList = Arrays.asList(testNotice);
        Page<NoticeDTO> noticePage = new PageImpl<>(noticeList, PageRequest.of(0, 10), 1);
        when(noticeService.getActiveNotices(any())).thenReturn(noticePage);

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
        when(noticeService.getNoticeDetail(1L)).thenReturn(testNotice);

        mockMvc.perform(get("/api/notice/{noticeNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("시스템 점검 안내"))
                .andExpect(jsonPath("$.content").value("서버 점검으로 인한 서비스 일시 중단 안내입니다."));
    }

    @Test
    @DisplayName("공지사항 등록 테스트")
    void createNotice() throws Exception {
        NoticeDTO newNotice = NoticeDTO.builder()
                .title("새로운 기능 출시 안내")
                .content("새로운 기능이 출시되었습니다.")
                .writer("관리자")
                .isImportant(false)
                .build();
        when(noticeService.createNotice(any(NoticeDTO.class))).thenReturn(1L);

        mockMvc.perform(post("/api/notice")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newNotice)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항이 등록되었습니다."))
                .andExpect(jsonPath("$.noticeId").value(1L));
    }

    @Test
    @DisplayName("공지사항 수정 테스트")
    void updateNotice() throws Exception {
        NoticeDTO updatedNotice = NoticeDTO.builder()
                .noticeNo(1L)
                .title("시스템 점검 완료 안내")
                .content("서버 점검이 완료되었습니다.")
                .writer("관리자")
                .isImportant(true)
                .isActive(true)
                .viewCount(10)
                .build();

        mockMvc.perform(put("/api/notice/{noticeNo}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedNotice)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항이 수정되었습니다."));
    }

    @Test
    @DisplayName("공지사항 삭제 테스트")
    void deleteNotice() throws Exception {
        mockMvc.perform(delete("/api/notice/{noticeNo}", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항이 삭제되었습니다."));
    }

    @Test
    @DisplayName("중요 공지사항 조회 테스트")
    void getImportantNotices() throws Exception {
        List<NoticeDTO> importantNotices = Arrays.asList(testNotice);
        when(noticeService.getImportantNotices()).thenReturn(importantNotices);

        mockMvc.perform(get("/api/notice/important"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].isImportant").value(true));
    }

    @Test
    @DisplayName("공지사항 검색 테스트")
    void searchNotices() throws Exception {
        List<NoticeDTO> searchResults = Arrays.asList(testNotice);
        Page<NoticeDTO> searchPage = new PageImpl<>(searchResults, PageRequest.of(0, 10), 1);
        when(noticeService.searchNotices(eq("점검"), any())).thenReturn(searchPage);

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
        List<NoticeDTO> recentNotices = Arrays.asList(testNotice);
        when(noticeService.getRecentNotices(5)).thenReturn(recentNotices);

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
        mockMvc.perform(put("/api/notice/{noticeNo}/toggle-status", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항 상태가 변경되었습니다."));
    }

    @Test
    @DisplayName("중요 공지 설정/해제 테스트")
    void toggleImportantStatus() throws Exception {
        mockMvc.perform(put("/api/notice/{noticeNo}/toggle-important", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항 중요도가 변경되었습니다."));
    }

    @Test
    @DisplayName("작성자별 공지사항 조회 테스트")
    void getNoticesByWriter() throws Exception {
        List<NoticeDTO> noticesByWriter = Arrays.asList(testNotice);
        when(noticeService.getNoticesByWriter("관리자")).thenReturn(noticesByWriter);

        mockMvc.perform(get("/api/notice/writer/{writer}", "관리자"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].writer").value("관리자"));
    }

    @Test
    @DisplayName("조회수 증가 테스트")
    void increaseViewCount() throws Exception {
        mockMvc.perform(post("/api/notice/{noticeNo}/view", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("조회수가 증가되었습니다."));
    }

    @Test
    @DisplayName("관리자용 모든 공지사항 조회 테스트")
    void getAllNoticesForAdmin() throws Exception {
        List<NoticeDTO> allNotices = Arrays.asList(testNotice);
        Page<NoticeDTO> noticePage = new PageImpl<>(allNotices, PageRequest.of(0, 10), 1);
        when(noticeService.getAllNoticesForAdmin(any())).thenReturn(noticePage);

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
        mockMvc.perform(delete("/api/notice/admin/{noticeNo}/permanent", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항이 완전히 삭제되었습니다."));
    }

    @Test
    @DisplayName("공지사항 통계 조회 테스트")
    void getNoticeStatistics() throws Exception {
        List<NoticeDTO> allNotices = Arrays.asList(testNotice);
        List<NoticeDTO> importantNotices = Arrays.asList(testNotice);
        when(noticeService.getAllActiveNotices()).thenReturn(allNotices);
        when(noticeService.getImportantNotices()).thenReturn(importantNotices);

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
        when(noticeService.getNoticeDetail(999L))
                .thenThrow(new RuntimeException("공지사항을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/notice/{noticeNo}", 999L))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 데이터로 공지사항 등록 시 오류 테스트")
    void createNoticeWithInvalidData() throws Exception {
        NoticeDTO invalidNotice = NoticeDTO.builder()
                .title("")
                .content("내용")
                .writer("관리자")
                .build();
        when(noticeService.createNotice(any(NoticeDTO.class)))
                .thenThrow(new RuntimeException("제목은 필수입니다."));

        mockMvc.perform(post("/api/notice")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidNotice)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
