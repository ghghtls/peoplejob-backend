package com.people.job.notice.service;

import com.people.job.notice.dto.NoticeDTO;
import com.people.job.notice.entity.NoticeEntity;
import com.people.job.notice.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("공지사항 서비스 테스트")
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private NoticeServiceImpl noticeService;

    private NoticeEntity testNoticeEntity;
    private NoticeDTO testNoticeDTO;

    @BeforeEach
    void setUp() {
        testNoticeEntity = NoticeEntity.builder()
                .noticeNo(1L)
                .title("시스템 점검 안내")
                .content("서버 점검으로 인한 서비스 일시 중단 안내입니다.")
                .author("관리자")
                .createdAt(LocalDateTime.now())
                .isImportant(true)
                .isPublished(true)
                .viewCount(0)
                .build();

        testNoticeDTO = NoticeDTO.builder()
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
    @DisplayName("모든 공지사항 조회 테스트")
    void findAll() {
        // Given
        List<NoticeEntity> noticeList = Arrays.asList(testNoticeEntity);
        Page<NoticeEntity> noticePage = new PageImpl<>(noticeList);
        Pageable pageable = PageRequest.of(0, 10);

        when(noticeRepository.findAll(pageable)).thenReturn(noticePage);

        // When
        Page<NoticeDTO> result = noticeService.findAll(null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("시스템 점검 안내", result.getContent().get(0).getTitle());
        verify(noticeRepository).findAll(pageable);
    }

    @Test
    @DisplayName("공지사항 ID로 조회 테스트")
    void findById() {
        // Given
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNoticeEntity));

        // When
        NoticeDTO result = noticeService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals("시스템 점검 안내", result.getTitle());
        assertEquals("서버 점검으로 인한 서비스 일시 중단 안내입니다.", result.getContent());
        verify(noticeRepository).findById(1L);
    }

    @Test
    @DisplayName("공지사항 저장 테스트")
    void save() {
        // Given
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(testNoticeEntity);

        // When
        NoticeDTO result = noticeService.save(testNoticeDTO);

        // Then
        assertNotNull(result);
        assertEquals("시스템 점검 안내", result.getTitle());
        verify(noticeRepository).save(any(NoticeEntity.class));
    }

    @Test
    @DisplayName("공지사항 수정 테스트")
    void update() {
        // Given
        NoticeEntity updatedEntity = NoticeEntity.builder()
                .noticeNo(1L)
                .title("시스템 점검 완료 안내")
                .content("서버 점검이 완료되었습니다.")
                .author("관리자")
                .createdAt(LocalDateTime.now())
                .isImportant(true)
                .isPublished(true)
                .viewCount(10)
                .build();

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNoticeEntity));
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(updatedEntity);

        NoticeDTO updateDTO = NoticeDTO.builder()
                .title("시스템 점검 완료 안내")
                .content("서버 점검이 완료되었습니다.")
                .build();

        // When
        NoticeDTO result = noticeService.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("시스템 점검 완료 안내", result.getTitle());
        assertEquals("서버 점검이 완료되었습니다.", result.getContent());
        verify(noticeRepository).findById(1L);
        verify(noticeRepository).save(any(NoticeEntity.class));
    }

    @Test
    @DisplayName("공지사항 삭제 테스트")
    void delete() {
        // Given
        when(noticeRepository.existsById(1L)).thenReturn(true);

        // When
        noticeService.delete(1L);

        // Then
        verify(noticeRepository).existsById(1L);
        verify(noticeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("중요 공지사항 조회 테스트")
    void findImportantNotices() {
        // Given
        List<NoticeEntity> importantNotices = Arrays.asList(testNoticeEntity);
        Page<NoticeEntity> noticePage = new PageImpl<>(importantNotices);
        Pageable pageable = PageRequest.of(0, 10);

        when(noticeRepository.findByIsImportantTrueAndIsPublishedTrue(pageable)).thenReturn(noticePage);

        // When
        Page<NoticeDTO> result = noticeService.findImportantNotices(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getIsImportant());
        verify(noticeRepository).findByIsImportantTrueAndIsPublishedTrue(pageable);
    }

    @Test
    @DisplayName("공지사항 검색 테스트")
    void search() {
        // Given
        List<NoticeEntity> searchResults = Arrays.asList(testNoticeEntity);
        Page<NoticeEntity> searchPage = new PageImpl<>(searchResults);
        Pageable pageable = PageRequest.of(0, 10);

        when(noticeRepository.findByTitleContainingOrContentContaining(
                eq("점검"), eq("점검"), eq(pageable))).thenReturn(searchPage);

        // When
        Page<NoticeDTO> result = noticeService.search("점검", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getTitle().contains("점검"));
        verify(noticeRepository).findByTitleContainingOrContentContaining("점검", "점검", pageable);
    }

    @Test
    @DisplayName("공지사항 게시 상태 토글 테스트")
    void togglePublishStatus() {
        // Given
        NoticeEntity unpublishedEntity = NoticeEntity.builder()
                .noticeNo(1L)
                .title("시스템 점검 안내")
                .content("서버 점검으로 인한 서비스 일시 중단 안내입니다.")
                .author("관리자")
                .createdAt(LocalDateTime.now())
                .isImportant(true)
                .isPublished(false)
                .viewCount(0)
                .build();

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNoticeEntity));
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(unpublishedEntity);

        // When
        NoticeDTO result = noticeService.togglePublishStatus(1L);

        // Then
        assertNotNull(result);
        assertFalse(result.getIsPublished());
        verify(noticeRepository).findById(1L);
        verify(noticeRepository).save(any(NoticeEntity.class));
    }

    @Test
    @DisplayName("조회수 증가 테스트")
    void incrementViewCount() {
        // Given
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNoticeEntity));
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(testNoticeEntity);

        // When
        noticeService.incrementViewCount(1L);

        // Then
        verify(noticeRepository).findById(1L);
        verify(noticeRepository).save(any(NoticeEntity.class));
    }
}