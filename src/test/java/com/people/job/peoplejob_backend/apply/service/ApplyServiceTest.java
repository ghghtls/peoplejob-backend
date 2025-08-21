package com.people.job.apply.service;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.entity.ApplyEntity;
import com.people.job.apply.repository.ApplyRepository;
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
@DisplayName("지원 서비스 테스트")
class ApplyServiceTest {

    @Mock
    private ApplyRepository applyRepository;

    @InjectMocks
    private ApplyServiceImpl applyService;

    private ApplyEntity testApplyEntity;
    private ApplyDTO testApplyDTO;

    @BeforeEach
    void setUp() {
        testApplyEntity = ApplyEntity.builder()
                .applyNo(1L)
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDateTime.now())
                .status(ApplyEntity.ApplyStatus.PENDING)
                .coverLetter("지원 동기입니다.")
                .build();

        testApplyDTO = ApplyDTO.builder()
                .applyNo(1L)
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDateTime.now())
                .status("PENDING")
                .coverLetter("지원 동기입니다.")
                .build();
    }

    @Test
    @DisplayName("지원서 제출 테스트")
    void submit() {
        // Given
        when(applyRepository.existsByJobNoAndUserNo(1L, 1L)).thenReturn(false);
        when(applyRepository.save(any(ApplyEntity.class))).thenReturn(testApplyEntity);

        // When
        ApplyDTO result = applyService.submit(testApplyDTO);

        // Then
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(applyRepository).existsByJobNoAndUserNo(1L, 1L);
        verify(applyRepository).save(any(ApplyEntity.class));
    }

    @Test
    @DisplayName("중복 지원 시 예외 발생 테스트")
    void submitDuplicate() {
        // Given
        when(applyRepository.existsByJobNoAndUserNo(1L, 1L)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> applyService.submit(testApplyDTO));
        verify(applyRepository).existsByJobNoAndUserNo(1L, 1L);
        verify(applyRepository, never()).save(any(ApplyEntity.class));
    }

    @Test
    @DisplayName("지원서 취소 테스트")
    void cancel() {
        // Given
        ApplyEntity cancelledEntity = ApplyEntity.builder()
                .applyNo(1L)
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDateTime.now())
                .status(ApplyEntity.ApplyStatus.CANCELLED)
                .coverLetter("지원 동기입니다.")
                .build();

        when(applyRepository.findById(1L)).thenReturn(Optional.of(testApplyEntity));
        when(applyRepository.save(any(ApplyEntity.class))).thenReturn(cancelledEntity);

        // When
        ApplyDTO result = applyService.cancel(1L);

        // Then
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        verify(applyRepository).findById(1L);
        verify(applyRepository).save(any(ApplyEntity.class));
    }

    @Test
    @DisplayName("지원 상태 변경 테스트")
    void updateStatus() {
        // Given
        ApplyEntity acceptedEntity = ApplyEntity.builder()
                .applyNo(1L)
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDateTime.now())
                .status(ApplyEntity.ApplyStatus.ACCEPTED)
                .coverLetter("지원 동기입니다.")
                .build();

        when(applyRepository.findById(1L)).thenReturn(Optional.of(testApplyEntity));
        when(applyRepository.save(any(ApplyEntity.class))).thenReturn(acceptedEntity);

        // When
        ApplyDTO result = applyService.updateStatus(1L, "ACCEPTED");

        // Then
        assertNotNull(result);
        assertEquals("ACCEPTED", result.getStatus());
        verify(applyRepository).findById(1L);
        verify(applyRepository).save(any(ApplyEntity.class));
    }

    @Test
    @DisplayName("사용자별 지원 목록 조회 테스트")
    void findByUserNo() {
        // Given
        List<ApplyEntity> applyList = Arrays.asList(testApplyEntity);
        Page<ApplyEntity> applyPage = new PageImpl<>(applyList);
        Pageable pageable = PageRequest.of(0, 10);

        when(applyRepository.findByUserNo(eq(1L), eq(pageable))).thenReturn(applyPage);

        // When
        Page<ApplyDTO> result = applyService.findByUserNo(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getUserNo());
        verify(applyRepository).findByUserNo(1L, pageable);
    }

    @Test
    @DisplayName("채용공고별 지원 목록 조회 테스트")
    void findByJobNo() {
        // Given
        List<ApplyEntity> applyList = Arrays.asList(testApplyEntity);
        Page<ApplyEntity> applyPage = new PageImpl<>(applyList);
        Pageable pageable = PageRequest.of(0, 10);

        when(applyRepository.findByJobNo(eq(1L), eq(pageable))).thenReturn(applyPage);

        // When
        Page<ApplyDTO> result = applyService.findByJobNo(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getJobNo());
        verify(applyRepository).findByJobNo(1L, pageable);
    }

    @Test
    @DisplayName("지원서 ID로 조회 테스트")
    void findById() {
        // Given
        when(applyRepository.findById(1L)).thenReturn(Optional.of(testApplyEntity));

        // When
        ApplyDTO result = applyService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getApplyNo());
        assertEquals("PENDING", result.getStatus());
        verify(applyRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 지원서 조회 시 예외 발생 테스트")
    void findByIdNotFound() {
        // Given
        when(applyRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> applyService.findById(999L));
        verify(applyRepository).findById(999L);
    }
}