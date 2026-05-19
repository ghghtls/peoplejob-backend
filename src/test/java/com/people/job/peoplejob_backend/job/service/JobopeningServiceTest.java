package com.people.job.peoplejob_backend.job.service;

import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.job.service.JobopeningServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("채용공고 서비스 테스트")
class JobopeningServiceTest {

    @MockitoBean
    private JobopeningRepository jobopeningRepository;

    @Autowired
    private JobopeningServiceImpl jobopeningService;

    private JobopeningEntity testJobEntity;
    private JobopeningDTO testJobDTO;

    @BeforeEach
    void setUp() {
        testJobEntity = JobopeningEntity.builder()
                .jobNo(1L)
                .title("백엔드 개발자 채용")
                .content("Spring Boot 기반 백엔드 개발자를 채용합니다.")
                .company("테스트 회사")
                .location("서울")
                .jobType("IT/소프트웨어")
                .salary("협의")
                .workType("정규직")
                .experience("3년 이상")
                .education("학력무관")
                .deadline(LocalDate.now().plusDays(30))
                .viewCount(0)
                .isActive(true)
                .userNo(1L)
                .status(JobopeningEntity.JobStatus.PUBLISHED)
                .build();

        // regdate와 updatedAt은 @PrePersist에서 설정되므로 수동 설정
        testJobEntity.setRegdate(LocalDateTime.now());
        testJobEntity.setUpdatedAt(LocalDateTime.now());

        testJobDTO = JobopeningDTO.builder()
                .jobNo(1L)
                .title("백엔드 개발자 채용")
                .content("Spring Boot 기반 백엔드 개발자를 채용합니다.")
                .company("테스트 회사")
                .location("서울")
                .jobType("IT/소프트웨어")
                .salary("협의")
                .workType("정규직")
                .experience("3년 이상")
                .education("학력무관")
                .deadline(LocalDate.now().plusDays(30))
                .regdate(LocalDate.now())
                .viewCount(0)
                .isActive(true)
                .userNo(1L)
                .status("PUBLISHED")
                .build();
    }

    @Test
    @DisplayName("채용공고 생성 테스트")
    void create() {
        // Given
        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(testJobEntity);

        // When
        JobopeningDTO result = jobopeningService.create(testJobDTO);

        // Then
        assertNotNull(result);
        assertEquals("백엔드 개발자 채용", result.getTitle());
        assertEquals("테스트 회사", result.getCompany());
        verify(jobopeningRepository).save(any(JobopeningEntity.class));
    }

    @Test
    @DisplayName("채용공고 ID로 조회 테스트")
    void getById() {
        // Given
        when(jobopeningRepository.findByJobNoAndIsActiveTrue(1L)).thenReturn(Optional.of(testJobEntity));
        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(testJobEntity);

        // When
        JobopeningDTO result = jobopeningService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals("백엔드 개발자 채용", result.getTitle());
        assertEquals("테스트 회사", result.getCompany());
        verify(jobopeningRepository).findByJobNoAndIsActiveTrue(1L);
    }

    @Test
    @DisplayName("채용공고 수정 테스트")
    void update() {
        // Given
        testJobEntity.setStatus(JobopeningEntity.JobStatus.DRAFT); // 수정 가능한 상태로 설정

        JobopeningEntity updatedEntity = JobopeningEntity.builder()
                .jobNo(1L)
                .title("시니어 백엔드 개발자 채용")
                .content("경험이 풍부한 시니어 백엔드 개발자를 채용합니다.")
                .company("테스트 회사")
                .location("서울")
                .jobType("IT/소프트웨어")
                .salary("5000만원~7000만원")
                .workType("정규직")
                .experience("5년 이상")
                .education("학력무관")
                .deadline(LocalDate.now().plusDays(30))
                .viewCount(10)
                .isActive(true)
                .userNo(1L)
                .status(JobopeningEntity.JobStatus.DRAFT)
                .build();

        when(jobopeningRepository.findByJobNoAndIsActiveTrue(1L)).thenReturn(Optional.of(testJobEntity));
        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(updatedEntity);

        JobopeningDTO updateDTO = JobopeningDTO.builder()
                .jobNo(1L)
                .title("시니어 백엔드 개발자 채용")
                .content("경험이 풍부한 시니어 백엔드 개발자를 채용합니다.")
                .salary("5000만원~7000만원")
                .experience("5년 이상")
                .build();

        // When
        JobopeningDTO result = jobopeningService.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("시니어 백엔드 개발자 채용", result.getTitle());
        assertEquals("5000만원~7000만원", result.getSalary());
        verify(jobopeningRepository).findByJobNoAndIsActiveTrue(1L);
        verify(jobopeningRepository).save(any(JobopeningEntity.class));
    }

    @Test
    @DisplayName("채용공고 삭제 테스트")
    void delete() {
        // Given
        testJobEntity.setStatus(JobopeningEntity.JobStatus.DRAFT); // 삭제 가능한 상태로 설정
        when(jobopeningRepository.findByJobNoAndIsActiveTrue(1L)).thenReturn(Optional.of(testJobEntity));
        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(testJobEntity);

        // When
        assertDoesNotThrow(() -> jobopeningService.delete(1L));

        // Then
        verify(jobopeningRepository).findByJobNoAndIsActiveTrue(1L);
        verify(jobopeningRepository).save(any(JobopeningEntity.class));
    }

    @Test
    @DisplayName("전체 채용공고 조회 테스트")
    void getAll() {
        // Given
        List<JobopeningEntity> jobList = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> jobPage = new PageImpl<>(jobList);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.findByIsActiveTrueOrderByRegdateDesc(pageable)).thenReturn(jobPage);

        // When
        Page<JobopeningDTO> result = jobopeningService.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("백엔드 개발자 채용", result.getContent().get(0).getTitle());
        verify(jobopeningRepository).findByIsActiveTrueOrderByRegdateDesc(pageable);
    }

    @Test
    @DisplayName("사용자별 채용공고 조회 테스트")
    void getByUser() {
        // Given
        List<JobopeningEntity> jobList = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> jobPage = new PageImpl<>(jobList);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.findByUserNoAndIsActiveTrueOrderByRegdateDesc(eq(1L), eq(pageable))).thenReturn(jobPage);

        // When
        Page<JobopeningDTO> result = jobopeningService.getByUser(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getUserNo());
        verify(jobopeningRepository).findByUserNoAndIsActiveTrueOrderByRegdateDesc(1L, pageable);
    }

    @Test
    @DisplayName("임시저장 테스트")
    void saveDraft() {
        // Given
        JobopeningEntity draftEntity = JobopeningEntity.builder()
                .jobNo(1L)
                .title("임시저장 채용공고")
                .content("임시저장된 내용입니다.")
                .company("테스트 회사")
                .location("서울")
                .userNo(1L)
                .status(JobopeningEntity.JobStatus.DRAFT)
                .isActive(true)
                .viewCount(0)
                .build();

        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(draftEntity);

        JobopeningDTO draftDTO = JobopeningDTO.builder()
                .title("임시저장 채용공고")
                .content("임시저장된 내용입니다.")
                .company("테스트 회사")
                .location("서울")
                .userNo(1L)
                .build();

        // When
        JobopeningDTO result = jobopeningService.saveDraft(draftDTO);

        // Then
        assertNotNull(result);
        assertEquals("DRAFT", result.getStatus());
        assertEquals("임시저장 채용공고", result.getTitle());
        verify(jobopeningRepository).save(any(JobopeningEntity.class));
    }

    @Test
    @DisplayName("사용자별 임시저장 목록 조회 테스트")
    void getDraftsByUser() {
        // Given
        testJobEntity.setStatus(JobopeningEntity.JobStatus.DRAFT);
        List<JobopeningEntity> draftList = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> draftPage = new PageImpl<>(draftList);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.findDraftsByUser(eq(1L), eq(JobopeningEntity.JobStatus.DRAFT), eq(pageable))).thenReturn(draftPage);

        // When
        Page<JobopeningDTO> result = jobopeningService.getDraftsByUser(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("DRAFT", result.getContent().get(0).getStatus());
        verify(jobopeningRepository).findDraftsByUser(1L, JobopeningEntity.JobStatus.DRAFT, pageable);
    }

    @Test
    @DisplayName("채용공고 게시 테스트")
    void publish() {
        // Given
        testJobEntity.setStatus(JobopeningEntity.JobStatus.DRAFT);
        JobopeningEntity publishedEntity = JobopeningEntity.builder()
                .jobNo(1L)
                .title("백엔드 개발자 채용")
                .content("Spring Boot 기반 백엔드 개발자를 채용합니다.")
                .company("테스트 회사")
                .location("서울")
                .deadline(LocalDate.now().plusDays(30))
                .userNo(1L)
                .status(JobopeningEntity.JobStatus.PUBLISHED)
                .isActive(true)
                .viewCount(0)
                .build();

        when(jobopeningRepository.findByJobNoAndIsActiveTrue(1L)).thenReturn(Optional.of(testJobEntity));
        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(publishedEntity);

        // When
        JobopeningDTO result = jobopeningService.publish(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals("PUBLISHED", result.getStatus());
        verify(jobopeningRepository).findByJobNoAndIsActiveTrue(1L);
        verify(jobopeningRepository).save(any(JobopeningEntity.class));
    }

    @Test
    @DisplayName("게시중인 채용공고만 조회 테스트")
    void getPublishedJobs() {
        // Given
        List<JobopeningEntity> publishedJobs = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> jobPage = new PageImpl<>(publishedJobs);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.findPublishedJobs(eq(JobopeningEntity.JobStatus.PUBLISHED), eq(pageable))).thenReturn(jobPage);

        // When
        Page<JobopeningDTO> result = jobopeningService.getPublishedJobs(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("PUBLISHED", result.getContent().get(0).getStatus());
        verify(jobopeningRepository).findPublishedJobs(JobopeningEntity.JobStatus.PUBLISHED, pageable);
    }

    @Test
    @DisplayName("사용자별 상태별 채용공고 조회 테스트")
    void getJobsByStatus() {
        // Given
        testJobEntity.setStatus(JobopeningEntity.JobStatus.DRAFT);
        List<JobopeningEntity> draftJobs = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> jobPage = new PageImpl<>(draftJobs);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.findByUserNoAndStatusAndIsActiveTrueOrderByRegdateDesc(
                eq(1L), eq(JobopeningEntity.JobStatus.DRAFT), eq(pageable))).thenReturn(jobPage);

        // When
        Page<JobopeningDTO> result = jobopeningService.getJobsByStatus(1L, "DRAFT", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getUserNo());
        verify(jobopeningRepository).findByUserNoAndStatusAndIsActiveTrueOrderByRegdateDesc(
                1L, JobopeningEntity.JobStatus.DRAFT, pageable);
    }

    @Test
    @DisplayName("사용자 채용공고 상태별 개수 조회 테스트")
    void getJobStatusCounts() {
        // Given
        when(jobopeningRepository.countByUserNoAndStatus(1L, JobopeningEntity.JobStatus.DRAFT)).thenReturn(5L);
        when(jobopeningRepository.countByUserNoAndStatus(1L, JobopeningEntity.JobStatus.PUBLISHED)).thenReturn(10L);
        when(jobopeningRepository.countByUserNoAndStatus(1L, JobopeningEntity.JobStatus.EXPIRED)).thenReturn(3L);
        when(jobopeningRepository.countByUserNoAndStatus(1L, JobopeningEntity.JobStatus.PENDING)).thenReturn(2L);
        when(jobopeningRepository.countByUserNoAndStatus(1L, JobopeningEntity.JobStatus.REJECTED)).thenReturn(1L);
        when(jobopeningRepository.countByUserNoAndStatus(1L, JobopeningEntity.JobStatus.SUSPENDED)).thenReturn(0L);

        // When
        Map<String, Long> result = jobopeningService.getJobStatusCounts(1L);

        // Then
        assertNotNull(result);
        assertEquals(5L, result.get("DRAFT"));
        assertEquals(10L, result.get("PUBLISHED"));
        assertEquals(3L, result.get("EXPIRED"));
        assertEquals(2L, result.get("PENDING"));
        assertEquals(1L, result.get("REJECTED"));
        assertEquals(0L, result.get("SUSPENDED"));
    }

    @Test
    @DisplayName("채용공고 상태 변경 테스트")
    void changeStatus() {
        // Given
        testJobEntity.setStatus(JobopeningEntity.JobStatus.PUBLISHED);
        JobopeningEntity expiredEntity = JobopeningEntity.builder()
                .jobNo(1L)
                .title("백엔드 개발자 채용")
                .company("테스트 회사")
                .userNo(1L)
                .status(JobopeningEntity.JobStatus.EXPIRED)
                .isActive(true)
                .viewCount(0)
                .build();

        when(jobopeningRepository.findByJobNoAndIsActiveTrue(1L)).thenReturn(Optional.of(testJobEntity));
        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(expiredEntity);

        // When
        JobopeningDTO result = jobopeningService.changeStatus(1L, "EXPIRED", 1L);

        // Then
        assertNotNull(result);
        assertEquals("EXPIRED", result.getStatus());
        verify(jobopeningRepository).findByJobNoAndIsActiveTrue(1L);
        verify(jobopeningRepository).save(any(JobopeningEntity.class));
    }

    @Test
    @DisplayName("마감일 지난 채용공고 자동 마감 처리 테스트")
    void expireOverdueJobs() {
        // Given
        List<JobopeningEntity> overdueJobs = Arrays.asList(testJobEntity);
        when(jobopeningRepository.findExpiredJobs(any(JobopeningEntity.JobStatus.class), any(LocalDate.class))).thenReturn(overdueJobs);
        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(testJobEntity);

        // When
        assertDoesNotThrow(() -> jobopeningService.expireOverdueJobs());

        // Then
        verify(jobopeningRepository).findExpiredJobs(any(JobopeningEntity.JobStatus.class), any(LocalDate.class));
        verify(jobopeningRepository).save(any(JobopeningEntity.class));
    }

    @Test
    @DisplayName("채용공고 검색 테스트")
    void searchJobs() {
        // Given
        List<JobopeningEntity> searchResults = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> searchPage = new PageImpl<>(searchResults);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.fullTextSearchPublishedJobs(eq("백엔드"), eq(pageable))).thenThrow(new RuntimeException("fulltext not available"));
        when(jobopeningRepository.searchPublishedJobs(eq("백엔드"), eq(JobopeningEntity.JobStatus.PUBLISHED), eq(pageable))).thenReturn(searchPage);

        // When
        Page<JobopeningDTO> result = jobopeningService.searchJobs("백엔드", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getTitle().contains("백엔드"));
        verify(jobopeningRepository).searchPublishedJobs("백엔드", JobopeningEntity.JobStatus.PUBLISHED, pageable);
    }

    @Test
    @DisplayName("카테고리별 채용공고 조회 테스트")
    void getJobsByCategory() {
        // Given
        List<JobopeningEntity> categoryJobs = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> jobPage = new PageImpl<>(categoryJobs);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.findPublishedJobsByCategory(
                eq(JobopeningEntity.JobStatus.PUBLISHED), eq("IT/소프트웨어"), eq("서울"), eq(pageable))).thenReturn(jobPage);

        // When
        Page<JobopeningDTO> result = jobopeningService.getJobsByCategory("IT/소프트웨어", "서울", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("IT/소프트웨어", result.getContent().get(0).getJobType());
        assertEquals("서울", result.getContent().get(0).getLocation());
        verify(jobopeningRepository).findPublishedJobsByCategory(JobopeningEntity.JobStatus.PUBLISHED, "IT/소프트웨어", "서울", pageable);
    }

    @Test
    @DisplayName("존재하지 않는 채용공고 조회 시 예외 발생 테스트")
    void getByIdNotFound() {
        // Given
        when(jobopeningRepository.findByJobNoAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> jobopeningService.getById(999L));
        verify(jobopeningRepository).findByJobNoAndIsActiveTrue(999L);
    }
}