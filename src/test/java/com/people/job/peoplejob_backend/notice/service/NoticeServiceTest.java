package com.people.job.peoplejob_backend.notice.service;

import com.people.job.notice.dto.NoticeDTO;
import com.people.job.notice.entity.NoticeEntity;
import com.people.job.notice.repository.NoticeRepository;
import com.people.job.notice.service.NoticeServiceImpl;
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

import java.time.LocalDate;
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
                .writer("관리자") // author -> writer로 수정
                .regdate(LocalDate.now()) // 실제 필드명과 타입
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isImportant(true)
                .isActive(true) // isPublished -> isActive로 수정
                .viewCount(0)
                .build();

        testNoticeDTO = NoticeDTO.builder()
                .noticeNo(1L)
                .title("시스템 점검 안내")
                .content("서버 점검으로 인한 서비스 일시 중단 안내입니다.")
                .writer("관리자") // author -> writer로 수정
                .regdate(LocalDate.now()) // 실제 필드명과 타입
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isImportant(true)
                .isActive(true) // isPublished -> isActive로 수정
                .viewCount(0)
                .build();
    }

    @Test
    @DisplayName("공지사항 생성 테스트")
    void createNotice() {
        // Given
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(testNoticeEntity);

        // When
        Long result = noticeService.createNotice(testNoticeDTO); // 실제 메서드명과 반환타입

        // Then
        assertNotNull(result);
        assertEquals(1L, result);
        verify(noticeRepository).save(any(NoticeEntity.class));
    }

    @Test
    @DisplayName("활성 공지사항 전체 조회 테스트")
    void getAllActiveNotices() {
        // Given
        List<NoticeEntity> noticeList = Arrays.asList(testNoticeEntity);
        when(noticeRepository.findByIsActiveTrueOrderByRegdateDesc()).thenReturn(noticeList);

        // When
        List<NoticeDTO> result = noticeService.getAllActiveNotices(); // 실제 메서드명

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("시스템 점검 안내", result.get(0).getTitle());
        verify(noticeRepository).findByIsActiveTrueOrderByRegdateDesc();
    }

    @Test
    @DisplayName("공지사항 페이징 조회 테스트")
    void getActiveNotices() {
        // Given
        List<NoticeEntity> noticeList = Arrays.asList(testNoticeEntity);
        Page<NoticeEntity> noticePage = new PageImpl<>(noticeList);
        Pageable pageable = PageRequest.of(0, 10);

        when(noticeRepository.findByIsActiveTrueOrderByRegdateDesc(pageable)).thenReturn(noticePage);

        // When
        Page<NoticeDTO> result = noticeService.getActiveNotices(pageable); // 실제 메서드명

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("시스템 점검 안내", result.getContent().get(0).getTitle());
        verify(noticeRepository).findByIsActiveTrueOrderByRegdateDesc(pageable);
    }

    @Test
    @DisplayName("공지사항 상세 조회 테스트 (조회수 증가 포함)")
    void getNoticeDetail() {
        // Given
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNoticeEntity));
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(testNoticeEntity);

        // When
        NoticeDTO result = noticeService.getNoticeDetail(1L); // 실제 메서드명

        // Then
        assertNotNull(result);
        assertEquals("시스템 점검 안내", result.getTitle());
        assertEquals("서버 점검으로 인한 서비스 일시 중단 안내입니다.", result.getContent());
        verify(noticeRepository).findById(1L);
        verify(noticeRepository).save(any(NoticeEntity.class)); // 조회수 증가를 위한 save 호출
    }

    @Test
    @DisplayName("공지사항 수정 테스트")
    void updateNotice() {
        // Given
        NoticeDTO updateDTO = NoticeDTO.builder()
                .noticeNo(1L)
                .title("시스템 점검 완료 안내")
                .content("서버 점검이 완료되었습니다.")
                .writer("관리자")
                .isImportant(true)
                .isActive(true)
                .build();

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNoticeEntity));
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(testNoticeEntity);

        // When
        assertDoesNotThrow(() -> noticeService.updateNotice(updateDTO)); // void 메서드

        // Then
        verify(noticeRepository).findById(1L);
        verify(noticeRepository).save(any(NoticeEntity.class));
    }

    @Test
    @DisplayName("공지사항 논리 삭제 테스트")
    void deleteNotice() {
        // Given
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNoticeEntity));
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(testNoticeEntity);

        // When
        assertDoesNotThrow(() -> noticeService.deleteNotice(1L)); // void 메서드

        // Then
        verify(noticeRepository).findById(1L);
        verify(noticeRepository).save(any(NoticeEntity.class)); // 논리삭제를 위한 save 호출
    }

    @Test
    @DisplayName("공지사항 물리 삭제 테스트")
    void permanentDeleteNotice() {
        // When
        assertDoesNotThrow(() -> noticeService.permanentDeleteNotice(1L));

        // Then
        verify(noticeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("중요 공지사항 조회 테스트")
    void getImportantNotices() {
        // Given
        List<NoticeEntity> importantNotices = Arrays.asList(testNoticeEntity);
        when(noticeRepository.findByIsImportantTrueAndIsActiveTrueOrderByRegdateDesc()).thenReturn(importantNotices);

        // When
        List<NoticeDTO> result = noticeService.getImportantNotices(); // 실제 메서드명과 반환타입

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsImportant());
        verify(noticeRepository).findByIsImportantTrueAndIsActiveTrueOrderByRegdateDesc();
    }

    @Test
    @DisplayName("공지사항 검색 테스트")
    void searchNotices() {
        // Given
        List<NoticeEntity> searchResults = Arrays.asList(testNoticeEntity);
        Page<NoticeEntity> searchPage = new PageImpl<>(searchResults);
        Pageable pageable = PageRequest.of(0, 10);

        when(noticeRepository.findByKeywordInTitleOrContent(eq("점검"), eq(pageable))).thenReturn(searchPage);

        // When
        Page<NoticeDTO> result = noticeService.searchNotices("점검", pageable); // 실제 메서드명

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getTitle().contains("점검"));
        verify(noticeRepository).findByKeywordInTitleOrContent("점검", pageable);
    }

    @Test
    @DisplayName("제목으로 검색 테스트")
    void searchByTitle() {
        // Given
        List<NoticeEntity> searchResults = Arrays.asList(testNoticeEntity);
        Page<NoticeEntity> searchPage = new PageImpl<>(searchResults);
        Pageable pageable = PageRequest.of(0, 10);

        when(noticeRepository.findByIsActiveTrueAndTitleContainingIgnoreCaseOrderByRegdateDesc(
                eq("점검"), eq(pageable))).thenReturn(searchPage);

        // When
        Page<NoticeDTO> result = noticeService.searchByTitle("점검", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(noticeRepository).findByIsActiveTrueAndTitleContainingIgnoreCaseOrderByRegdateDesc("점검", pageable);
    }

    @Test
    @DisplayName("내용으로 검색 테스트")
    void searchByContent() {
        // Given
        List<NoticeEntity> searchResults = Arrays.asList(testNoticeEntity);
        Page<NoticeEntity> searchPage = new PageImpl<>(searchResults);
        Pageable pageable = PageRequest.of(0, 10);

        when(noticeRepository.findByIsActiveTrueAndContentContainingIgnoreCaseOrderByRegdateDesc(
                eq("서비스"), eq(pageable))).thenReturn(searchPage);

        // When
        Page<NoticeDTO> result = noticeService.searchByContent("서비스", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(noticeRepository).findByIsActiveTrueAndContentContainingIgnoreCaseOrderByRegdateDesc("서비스", pageable);
    }

    @Test
    @DisplayName("조회수 증가 테스트")
    void increaseViewCount() {
        // When
        assertDoesNotThrow(() -> noticeService.increaseViewCount(1L)); // void 메서드

        // Then
        verify(noticeRepository).incrementViewCount(1L); // 실제 Repository 메서드
    }

    @Test
    @DisplayName("최근 공지사항 조회 테스트")
    void getRecentNotices() {
        // Given
        List<NoticeEntity> recentNotices = Arrays.asList(testNoticeEntity);
        when(noticeRepository.findRecentNotices(any(Pageable.class))).thenReturn(recentNotices);

        // When
        List<NoticeDTO> result = noticeService.getRecentNotices(5);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(noticeRepository).findRecentNotices(any(Pageable.class));
    }

    @Test
    @DisplayName("관리자용 모든 공지사항 조회 테스트")
    void getAllNoticesForAdmin() {
        // Given
        List<NoticeEntity> allNotices = Arrays.asList(testNoticeEntity);
        Page<NoticeEntity> noticePage = new PageImpl<>(allNotices);
        Pageable pageable = PageRequest.of(0, 10);

        when(noticeRepository.findAllByOrderByRegdateDesc(pageable)).thenReturn(noticePage);

        // When
        Page<NoticeDTO> result = noticeService.getAllNoticesForAdmin(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(noticeRepository).findAllByOrderByRegdateDesc(pageable);
    }

    @Test
    @DisplayName("작성자별 공지사항 조회 테스트")
    void getNoticesByWriter() {
        // Given
        List<NoticeEntity> writerNotices = Arrays.asList(testNoticeEntity);
        when(noticeRepository.findByWriterOrderByRegdateDesc("관리자")).thenReturn(writerNotices);

        // When
        List<NoticeDTO> result = noticeService.getNoticesByWriter("관리자");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("관리자", result.get(0).getWriter());
        verify(noticeRepository).findByWriterOrderByRegdateDesc("관리자");
    }

    @Test
    @DisplayName("공지사항 활성화 상태 토글 테스트")
    void toggleNoticeStatus() {
        // Given
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNoticeEntity));
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(testNoticeEntity);

        // When
        assertDoesNotThrow(() -> noticeService.toggleNoticeStatus(1L)); // 실제 메서드명

        // Then
        verify(noticeRepository).findById(1L);
        verify(noticeRepository).save(any(NoticeEntity.class));
    }

    @Test
    @DisplayName("중요 공지 설정 토글 테스트")
    void toggleImportantStatus() {
        // Given
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNoticeEntity));
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(testNoticeEntity);

        // When
        assertDoesNotThrow(() -> noticeService.toggleImportantStatus(1L));

        // Then
        verify(noticeRepository).findById(1L);
        verify(noticeRepository).save(any(NoticeEntity.class));
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 조회 시 예외 발생 테스트")
    void getNoticeDetailNotFound() {
        // Given
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> noticeService.getNoticeDetail(999L));
        assertTrue(exception.getMessage().contains("조회에 실패") || exception.getMessage().contains("찾을 수 없습니다"));
        verify(noticeRepository).findById(999L);
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 수정 시 예외 발생 테스트")
    void updateNoticeNotFound() {
        // Given
        testNoticeDTO.setNoticeNo(999L);
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> noticeService.updateNotice(testNoticeDTO));
        assertTrue(exception.getMessage().contains("수정") || exception.getMessage().contains("찾을 수 없습니다"));
        verify(noticeRepository).findById(999L);
        verify(noticeRepository, never()).save(any(NoticeEntity.class));
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 삭제 시 예외 발생 테스트")
    void deleteNoticeNotFound() {
        // Given
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> noticeService.deleteNotice(999L));
        assertTrue(exception.getMessage().contains("삭제") || exception.getMessage().contains("찾을 수 없습니다"));
        verify(noticeRepository).findById(999L);
        verify(noticeRepository, never()).save(any(NoticeEntity.class));
    }
}