package com.people.job.notice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.notice.dto.NoticeDTO;
import com.people.job.notice.service.NoticeService;
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
@DisplayName("공지사항 컨트롤러 테스트")
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoticeService noticeService;

    private NoticeDTO testNotice;

    @BeforeEach
    void setUp() {
        testNotice = NoticeDTO.builder()
                .noticeNo(1L)
                .title("시스템 점검 안내")
                .content("서버 점검으로 인한 서비스 일시 중단 안내입니다.")
                .author("관리자")
                .createdAt(LocalDateTime.now())
                .isImportant(true)
                .isPublished(true)
                .viewCount(0)
                .build();
    }

    @Test
    @DisplayName("공지사항 목록 조회 테스트")
    void getNoticeList() throws Exception {
        // Given
        List<NoticeDTO> noticeList = Arrays.asList(testNotice);
        Page<NoticeDTO> noticePage = new PageImpl<>(noticeList, PageRequest.of(0, 10), 1);

        when(noticeService.findAll(any(), any())).thenReturn(noticePage);

        // When & Then
        mockMvc.perform(get("/api/notices")
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
        when(noticeService.findById(1L)).thenReturn(testNotice);

        // When & Then
        mockMvc.perform(get("/api/notices/{id}", 1L))
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
                .author("관리자")
                .isImportant(false)
                .build();

        when(noticeService.save(any(NoticeDTO.class))).thenReturn(testNotice);

        // When & Then
        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newNotice)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("시스템 점검 안내"));
    }

    @Test
    @DisplayName("공지사항 수정 테스트")
    void updateNotice() throws Exception {
        // Given
        NoticeDTO updatedNotice = NoticeDTO.builder()
                .noticeNo(1L)
                .title("시스템 점검 완료 안내")
                .content("서버 점검이 완료되었습니다.")
                .author("관리자")
                .isImportant(true)
                .isPublished(true)
                .viewCount(10)
                .build();

        when(noticeService.update(eq(1L), any(NoticeDTO.class))).thenReturn(updatedNotice);

        // When & Then
        mockMvc.perform(put("/api/notices/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedNotice)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("시스템 점검 완료 안내"));
    }

    @Test
    @DisplayName("공지사항 삭제 테스트")
    void deleteNotice() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/notices/{id}", 1L))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("중요 공지사항 조회 테스트")
    void getImportantNotices() throws Exception {
        // Given
        List<NoticeDTO> importantNotices = Arrays.asList(testNotice);
        Page<NoticeDTO> noticePage = new PageImpl<>(importantNotices, PageRequest.of(0, 10), 1);

        when(noticeService.findImportantNotices(any())).thenReturn(noticePage);

        // When & Then
        mockMvc.perform(get("/api/notices/important")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].isImportant").value(true));
    }

    @Test
    @DisplayName("공지사항 검색 테스트")
    void searchNotices() throws Exception {
        // Given
        List<NoticeDTO> searchResults = Arrays.asList(testNotice);
        Page<NoticeDTO> searchPage = new PageImpl<>(searchResults, PageRequest.of(0, 10), 1);

        when(noticeService.search(eq("점검"), any())).thenReturn(searchPage);

        // When & Then
        mockMvc.perform(get("/api/notices/search")
                        .param("keyword", "점검")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("시스템 점검 안내"));
    }

    @Test
    @DisplayName("공지사항 게시/비게시 상태 변경 테스트")
    void togglePublishStatus() throws Exception {
        // Given
        NoticeDTO unpublishedNotice = NoticeDTO.builder()
                .noticeNo(1L)
                .title("시스템 점검 안내")
                .content("서버 점검으로 인한 서비스 일시 중단 안내입니다.")
                .author("관리자")
                .isImportant(true)
                .isPublished(false)
                .viewCount(0)
                .build();

        when(noticeService.togglePublishStatus(1L)).thenReturn(unpublishedNotice);

        // When & Then
        mockMvc.perform(patch("/api/notices/{id}/toggle-publish", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPublished").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 조회 시 404 에러 테스트")
    void getNoticeNotFound() throws Exception {
        // Given
        when(noticeService.findById(999L))
                .thenThrow(new RuntimeException("공지사항을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/notices/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("잘못된 데이터로 공지사항 등록 시 400 에러 테스트")
    void createNoticeWithInvalidData() throws Exception {
        // Given
        NoticeDTO invalidNotice = NoticeDTO.builder()
                .title("") // 빈 제목
                .content("내용")
                .build();

        // When & Then
        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidNotice)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}