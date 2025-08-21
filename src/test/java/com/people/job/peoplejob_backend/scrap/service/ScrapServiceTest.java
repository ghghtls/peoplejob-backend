package com.people.job.scrap.service;

import com.people.job.scrap.dto.ScrapDTO;
import com.people.job.scrap.entity.ScrapEntity;
import com.people.job.scrap.repository.ScrapRepository;
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
@DisplayName("스크랩 서비스 테스트")
class ScrapServiceTest {

    @Mock
    private ScrapRepository scrapRepository;

    @InjectMocks
    private ScrapServiceImpl scrapService;

    private ScrapEntity testScrapEntity;
    private ScrapDTO testScrapDTO;

    @BeforeEach
    void setUp() {
        testScrapEntity = ScrapEntity.builder()
                .scrapNo(1L)
                .userNo(1L)
                .jobNo(1L)
                .scrapDate(LocalDateTime.now())
                .build();

        testScrapDTO = ScrapDTO.builder()
                .scrapNo(1L)
                .userNo(1L)
                .jobNo(1L)
                .scrapDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("스크랩 추가 테스트")
    void addScrap() {
        // Given
        when(scrapRepository.existsByUserNoAndJobNo(1L, 1L)).thenReturn(false);
        when(scrapRepository.save(any(ScrapEntity.class))).thenReturn(testScrapEntity);

        // When
        ScrapDTO result = scrapService.addScrap(testScrapDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserNo());
        assertEquals(1L, result.getJobNo());
        verify(scrapRepository).existsByUserNoAndJobNo(1L, 1L);
        verify(scrapRepository).save(any(ScrapEntity.class));
    }

    @Test
    @DisplayName("중복 스크랩 시 예외 발생 테스트")
    void addDuplicateScrap() {
        // Given
        when(scrapRepository.existsByUserNoAndJobNo(1L, 1L)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> scrapService.addScrap(testScrapDTO));
        verify(scrapRepository).existsByUserNoAndJobNo(1L, 1L);
        verify(scrapRepository, never()).save(any(ScrapEntity.class));
    }

    @Test
    @DisplayName("스크랩 삭제 테스트")
    void removeScrap() {
        // Given
        when(scrapRepository.existsById(1L)).thenReturn(true);

        // When
        scrapService.removeScrap(1L);

        // Then
        verify(scrapRepository).existsById(1L);
        verify(scrapRepository).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 스크랩 삭제 시 예외 발생 테스트")
    void removeNonExistentScrap() {
        // Given
        when(scrapRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> scrapService.removeScrap(999L));
        verify(scrapRepository).existsById(999L);
        verify(scrapRepository, never()).deleteById(999L);
    }

    @Test
    @DisplayName("사용자별 스크랩 목록 조회 테스트")
    void findByUserNo() {
        // Given
        List<ScrapEntity> scrapList = Arrays.asList(testScrapEntity);
        Page<ScrapEntity> scrapPage = new PageImpl<>(scrapList);
        Pageable pageable = PageRequest.of(0, 10);

        when(scrapRepository.findByUserNo(eq(1L), eq(pageable))).thenReturn(scrapPage);

        // When
        Page<ScrapDTO> result = scrapService.findByUserNo(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getUserNo());
        verify(scrapRepository).findByUserNo(1L, pageable);
    }

    @Test
    @DisplayName("채용공고별 스크랩 개수 조회 테스트")
    void countByJobNo() {
        // Given
        when(scrapRepository.countByJobNo(1L)).thenReturn(5L);

        // When
        Long result = scrapService.countByJobNo(1L);

        // Then
        assertEquals(5L, result);
        verify(scrapRepository).countByJobNo(1L);
    }

    @Test
    @DisplayName("스크랩 여부 확인 테스트")
    void isScraped() {
        // Given
        when(scrapRepository.existsByUserNoAndJobNo(1L, 1L)).thenReturn(true);

        // When
        boolean result = scrapService.isScraped(1L, 1L);

        // Then
        assertTrue(result);
        verify(scrapRepository).existsByUserNoAndJobNo(1L, 1L);
    }

    @Test
    @DisplayName("스크랩 ID로 조회 테스트")
    void findById() {
        // Given
        when(scrapRepository.findById(1L)).thenReturn(Optional.of(testScrapEntity));

        // When
        ScrapDTO result = scrapService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getScrapNo());
        verify(scrapRepository).findById(1L);
    }

    @Test
    @DisplayName("사용자와 채용공고로 스크랩 조회 테스트")
    void findByUserNoAndJobNo() {
        // Given
        when(scrapRepository.findByUserNoAndJobNo(1L, 1L)).thenReturn(Optional.of(testScrapEntity));

        // When
        ScrapDTO result = scrapService.findByUserNoAndJobNo(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserNo());
        assertEquals(1L, result.getJobNo());
        verify(scrapRepository).findByUserNoAndJobNo(1L, 1L);
    }

    @Test
    @DisplayName("사용자의 모든 스크랩 삭제 테스트")
    void removeAllScrapsByUser() {
        // When
        scrapService.removeAllScrapsByUser(1L);

        // Then
        verify(scrapRepository).deleteByUserNo(1L);
    }

    @Test
    @DisplayName("채용공고의 모든 스크랩 삭제 테스트")
    void removeAllScrapsByJob() {
        // When
        scrapService.removeAllScrapsByJob(1L);

        // Then
        verify(scrapRepository).deleteByJobNo(1L);
    }
}