package com.people.job.peoplejob_backend.scrap.service;

import com.people.job.job.repository.JobopeningRepository;
import com.people.job.scrap.dto.ScrapDTO;
import com.people.job.scrap.entity.ScrapEntity;
import com.people.job.scrap.repository.ScrapRepository;
import com.people.job.scrap.service.ScrapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    @Mock
    private JobopeningRepository jobopeningRepository;

    @InjectMocks
    private ScrapServiceImpl scrapService;

    private ScrapEntity testScrapEntity;
    private ScrapDTO testScrapDTO;

    @BeforeEach
    void setUp() {
        testScrapEntity = ScrapEntity.builder()
                .scrapNo(1L)
                .userNo(1L)
                .jobNo(1L) // jobopeningNo -> jobNo로 수정
                .scrapDate(LocalDate.now()) // LocalDateTime -> LocalDate로 수정
                .build();

        testScrapDTO = ScrapDTO.builder()
                .scrapNo(1L)
                .userNo(1L)
                .jobNo(1L) // jobopeningNo -> jobNo로 수정
                .scrapDate(LocalDate.now()) // LocalDateTime -> LocalDate로 수정
                .build();
    }

    @Test
    @DisplayName("스크랩 추가 테스트")
    void addScrap() {
        // Given
        when(scrapRepository.existsByUserNoAndJobNo(1L, 1L)).thenReturn(false); // 실제 메서드명으로 수정
        when(scrapRepository.save(any(ScrapEntity.class))).thenReturn(testScrapEntity);

        // When
        assertDoesNotThrow(() -> scrapService.addScrap(testScrapDTO)); // void 메서드이므로 수정

        // Then
        verify(scrapRepository).existsByUserNoAndJobNo(1L, 1L);
        verify(scrapRepository).save(any(ScrapEntity.class));
    }

    @Test
    @DisplayName("중복 스크랩 시 예외 발생 테스트")
    void addDuplicateScrap() {
        // Given
        when(scrapRepository.existsByUserNoAndJobNo(1L, 1L)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> scrapService.addScrap(testScrapDTO));
        assertEquals("이미 스크랩한 공고입니다.", exception.getMessage()); // 실제 메시지로 수정
        verify(scrapRepository).existsByUserNoAndJobNo(1L, 1L);
        verify(scrapRepository, never()).save(any(ScrapEntity.class));
    }

    @Test
    @DisplayName("스크랩 삭제 테스트")
    void removeScrap() {
        // When
        assertDoesNotThrow(() -> scrapService.deleteScrap(1L)); // 실제 메서드명으로 수정

        // Then
        verify(scrapRepository).deleteById(1L);
    }

    @Test
    @DisplayName("사용자별 스크랩 목록 조회 테스트")
    void findByUserNo() {
        // Given
        List<ScrapEntity> scrapList = Arrays.asList(testScrapEntity);
        when(scrapRepository.findByUserNo(1L)).thenReturn(scrapList); // Pageable 제거

        // When
        List<ScrapDTO> result = scrapService.getScrapsByUser(1L); // 실제 메서드명으로 수정

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserNo());
        verify(scrapRepository).findByUserNo(1L);
    }

    @Test
    @DisplayName("사용자와 채용공고로 스크랩 삭제 테스트")
    void deleteScrapByUserAndJob() {
        // Given
        when(scrapRepository.findByUserNoAndJobNo(1L, 1L)).thenReturn(Optional.of(testScrapEntity));

        // When
        assertDoesNotThrow(() -> scrapService.deleteScrapByUserAndJob(1L, 1L)); // 실제 메서드명으로 수정

        // Then
        verify(scrapRepository).findByUserNoAndJobNo(1L, 1L);
        verify(scrapRepository).delete(testScrapEntity);
    }

    @Test
    @DisplayName("존재하지 않는 스크랩 삭제 시 예외 발생 테스트")
    void deleteNonExistentScrapByUserAndJob() {
        // Given
        when(scrapRepository.findByUserNoAndJobNo(1L, 999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> scrapService.deleteScrapByUserAndJob(1L, 999L));
        assertEquals("스크랩 내역이 없습니다.", exception.getMessage()); // 실제 메시지로 수정
        verify(scrapRepository).findByUserNoAndJobNo(1L, 999L);
        verify(scrapRepository, never()).delete(any(ScrapEntity.class));
    }

    @Test
    @DisplayName("빈 스크랩 목록 조회 테스트")
    void getEmptyScrapsByUser() {
        // Given
        when(scrapRepository.findByUserNo(1L)).thenReturn(Arrays.asList());

        // When
        List<ScrapDTO> result = scrapService.getScrapsByUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(scrapRepository).findByUserNo(1L);
    }

    @Test
    @DisplayName("유효하지 않은 스크랩 데이터 추가 테스트")
    void addInvalidScrap() {
        // Given
        ScrapDTO invalidScrap = ScrapDTO.builder()
                .userNo(null) // 필수 값 누락
                .jobNo(1L)
                .build();

        // When & Then
        // Service에서 검증 로직이 없다면 NullPointerException이 발생할 수 있음
        assertThrows(Exception.class, () -> scrapService.addScrap(invalidScrap));
    }
}