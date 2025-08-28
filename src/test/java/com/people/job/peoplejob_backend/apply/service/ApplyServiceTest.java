package com.people.job.peoplejob_backend.apply.service;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.entity.ApplyEntity;
import com.people.job.apply.repository.ApplyRepository;
import com.people.job.apply.service.ApplyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("지원 서비스 테스트")
class ApplyServiceTest {

    @MockitoBean
    private ApplyRepository applyRepository;

    @Autowired
    private ApplyServiceImpl applyService;

    private ApplyEntity testApplyEntity;
    private ApplyDTO testApplyDTO;

    @BeforeEach
    void setUp() {
        testApplyEntity = ApplyEntity.builder()
                .applyNo(1L)
                .jobNo(1L) // 실제 Entity 필드명
                .userNo(1L) // 실제 Entity 필드명
                .resumeNo(1L)
                .applyDate(LocalDate.now()) // 실제 Entity 필드명과 타입
                .status("PENDING")
                .message("지원합니다.")
                .build();

        testApplyDTO = ApplyDTO.builder()
                .applyNo(1L)
                .jobNo(1L) // 실제 DTO 필드명
                .userNo(1L) // 실제 DTO 필드명
                .resumeNo(1L)
                .applyDate(LocalDate.now()) // 실제 DTO 필드명과 타입
                .status("PENDING")
                .message("지원합니다.")
                .build();
    }

    @Test
    @DisplayName("지원하기 성공 테스트")
    void applyToJobSuccess() {
        // Given - 중복 지원이 아닌 경우
        when(applyRepository.existsByResumeNoAndJobNo(1L, 1L)).thenReturn(false); // 실제 Repository 메서드명
        when(applyRepository.save(any(ApplyEntity.class))).thenReturn(testApplyEntity);

        // When
        assertDoesNotThrow(() -> applyService.applyToJob(testApplyDTO));

        // Then
        verify(applyRepository).existsByResumeNoAndJobNo(1L, 1L);
        verify(applyRepository).save(any(ApplyEntity.class));
    }

    @Test
    @DisplayName("중복 지원 시 예외 발생 테스트")
    void applyToJobDuplicate() {
        // Given - 이미 지원한 경우
        when(applyRepository.existsByResumeNoAndJobNo(1L, 1L)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> applyService.applyToJob(testApplyDTO));
        assertEquals("이미 지원한 공고입니다.", exception.getMessage());

        verify(applyRepository).existsByResumeNoAndJobNo(1L, 1L);
        verify(applyRepository, never()).save(any(ApplyEntity.class));
    }

    @Test
    @DisplayName("이력서별 지원 내역 조회 테스트")
    void getAppliesByResume() {
        // Given
        List<ApplyEntity> applyList = Arrays.asList(testApplyEntity);
        when(applyRepository.findByResumeNo(1L)).thenReturn(applyList);

        // When
        List<ApplyDTO> result = applyService.getAppliesByResume(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getResumeNo());
        verify(applyRepository).findByResumeNo(1L);
    }

    @Test
    @DisplayName("채용공고별 지원자 목록 조회 테스트")
    void getAppliesByJobopening() {
        // Given
        List<ApplyEntity> applyList = Arrays.asList(testApplyEntity);
        when(applyRepository.findByJobNo(1L)).thenReturn(applyList); // 실제 Repository 메서드명

        // When
        List<ApplyDTO> result = applyService.getAppliesByJobopening(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getJobNo());
        verify(applyRepository).findByJobNo(1L);
    }

    @Test
    @DisplayName("지원 취소 테스트")
    void cancelApply() {
        // Given
        doNothing().when(applyRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> applyService.cancelApply(1L));

        // Then
        verify(applyRepository).deleteById(1L);
    }

    @Test
    @DisplayName("빈 지원 목록 조회 테스트 - 이력서별")
    void getEmptyAppliesByResume() {
        // Given
        when(applyRepository.findByResumeNo(999L)).thenReturn(Arrays.asList());

        // When
        List<ApplyDTO> result = applyService.getAppliesByResume(999L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(applyRepository).findByResumeNo(999L);
    }

    @Test
    @DisplayName("빈 지원자 목록 조회 테스트 - 채용공고별")
    void getEmptyAppliesByJobopening() {
        // Given
        when(applyRepository.findByJobNo(999L)).thenReturn(Arrays.asList());

        // When
        List<ApplyDTO> result = applyService.getAppliesByJobopening(999L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(applyRepository).findByJobNo(999L);
    }

    @Test
    @DisplayName("유효하지 않은 지원 데이터 테스트")
    void applyToJobWithInvalidData() {
        // Given - 필수 필드 누락
        ApplyDTO invalidApply = ApplyDTO.builder()
                .resumeNo(null) // 필수 값 누락
                .jobNo(1L)
                .userNo(1L)
                .build();

        // When & Then
        // Service에서 검증 로직이 없다면 NullPointerException이 발생할 수 있음
        assertThrows(Exception.class, () -> applyService.applyToJob(invalidApply));
    }

    @Test
    @DisplayName("여러 지원 내역 조회 테스트")
    void getMultipleAppliesByResume() {
        // Given
        ApplyEntity apply2 = ApplyEntity.builder()
                .applyNo(2L)
                .jobNo(2L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDate.now().minusDays(1))
                .status("REVIEWED")
                .message("두 번째 지원입니다.")
                .build();

        List<ApplyEntity> applyList = Arrays.asList(testApplyEntity, apply2);
        when(applyRepository.findByResumeNo(1L)).thenReturn(applyList);

        // When
        List<ApplyDTO> result = applyService.getAppliesByResume(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getResumeNo());
        assertEquals(1L, result.get(1).getResumeNo());
        verify(applyRepository).findByResumeNo(1L);
    }
}