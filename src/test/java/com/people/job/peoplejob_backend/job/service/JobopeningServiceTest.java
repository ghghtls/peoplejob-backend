package com.people.job.job.service;

import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
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
@DisplayName("채용공고 서비스 테스트")
class JobopeningServiceTest {

    @Mock
    private JobopeningRepository jobopeningRepository;

    @InjectMocks
    private JobopeningServiceImpl jobopeningService;

    private JobopeningEntity testJobEntity;
    private JobopeningDTO testJobDTO;

    @BeforeEach
    void setUp() {
        testJobEntity = JobopeningEntity.builder()
                .jobNo(1L)
                .title("백엔드 개발자 모집")
                .content("Java/Spring Boot 개발자를 모집합니다.")
                .company("테스트회사")
                .location("서울시 강남구")
                .jobType("정규직")
                .salary("협의")
                .workType("오프라인")
                .experience("경력무관")
                .education("대졸")
                .deadline(LocalDate.now().plusDays(30))
                .regdate(LocalDateTime.now())
                .viewCount(0)
                .isActive(true)
                .userNo(1L)
                .status(JobopeningEntity.JobStatus.PUBLISHED)
                .build();

        testJobDTO = JobopeningDTO.builder()
                .jobNo(1L)
                .title("백엔드 개발자 모집")
                .content("Java/Spring Boot 개발자를 모집합니다.")
                .company("테스트회사")
                .location("서울시 강남구")
                .jobType("정규직")
                .salary("협의")
                .workType("오프라인")
                .experience("경력무관")
                .education("대졸")
                .deadline(LocalDate.now().plusDays(30))
                .regdate(LocalDateTime.now())
                .viewCount(0)
                .isActive(true)
                .userNo(1L)
                .status("PUBLISHED")
                .build();
    }

    @Test
    @DisplayName("모든 채용공고 조회 테스트")
    void findAll() {
        // Given
        List<JobopeningEntity> jobList = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> jobPage = new PageImpl<>(jobList);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.findAll(pageable)).thenReturn(jobPage);

        // When
        Page<JobopeningDTO> result = jobopeningService.findAll(null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("백엔드 개발자 모집", result.getContent().get(0).getTitle());
        verify(jobopeningRepository).findAll(pageable);
    }

    @Test
    @DisplayName("채용공고 ID로 조회 테스트")
    void findById() {
        // Given
        when(jobopeningRepository.findById(1L)).thenReturn(Optional.of(testJobEntity));

        // When
        JobopeningDTO result = jobopeningService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals("백엔드 개발자 모집", result.getTitle());
        assertEquals("테스트회사", result.getCompany());
        verify(jobopeningRepository).findById(1L);
    }

    @Test
    @DisplayName("채용공고 저장 테스트")
    void save() {
        // Given
        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(testJobEntity);

        // When
        JobopeningDTO result = jobopeningService.save(testJobDTO);

        // Then
        assertNotNull(result);
        assertEquals("백엔드 개발자 모집", result.getTitle());
        verify(jobopeningRepository).save(any(JobopeningEntity.class));
    }

    @Test
    @DisplayName("채용공고 수정 테스트")
    void update() {
        // Given
        JobopeningEntity updatedEntity = JobopeningEntity.builder()
                .jobNo(1L)
                .title("시니어 백엔드 개발자 모집")
                .content("경험 많은 Java/Spring Boot 개발자를 모집합니다.")
                .company("테스트회사")
                .location("서울시 강남구")
                .jobType("정규직")
                .salary("5000만원")
                .workType("하이브리드")
                .experience("5년 이상")
                .education("대졸")
                .deadline(LocalDate.now().plusDays(30))
                .regdate(LocalDateTime.now())
                .viewCount(5)
                .isActive(true)
                .userNo(1L)
                .status(JobopeningEntity.JobStatus.PUBLISHED)
                .build();

        when(jobopeningRepository.findById(1L)).thenReturn(Optional.of(testJobEntity));
        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(updatedEntity);

        JobopeningDTO updateDTO = JobopeningDTO.builder()
                .title("시니어 백엔드 개발자 모집")
                .content("경험 많은 Java/Spring Boot 개발자를 모집합니다.")
                .salary("5000만원")
                .experience("5년 이상")
                .build();

        // When
        JobopeningDTO result = jobopeningService.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("시니어 백엔드 개발자 모집", result.getTitle());
        assertEquals("5000만원", result.getSalary());
        verify(jobopeningRepository).findById(1L);
        verify(jobopeningRepository).save(any(JobopeningEntity.class));
    }

    @Test
    @DisplayName("채용공고 삭제 테스트")
    void delete() {
        // Given
        when(jobopeningRepository.existsById(1L)).thenReturn(true);

        // When
        jobopeningService.delete(1L);

        // Then
        verify(jobopeningRepository).existsById(1L);
        verify(jobopeningRepository).deleteById(1L);
    }

    @Test
    @DisplayName("회사별 채용공고 조회 테스트")
    void findByCompany() {
        // Given
        List<JobopeningEntity> jobList = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> jobPage = new PageImpl<>(jobList);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.findByCompany(eq("테스트회사"), eq(pageable))).thenReturn(jobPage);

        // When
        Page<JobopeningDTO> result = jobopeningService.findByCompany("테스트회사", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("테스트회사", result.getContent().get(0).getCompany());
        verify(jobopeningRepository).findByCompany("테스트회사", pageable);
    }

    @Test
    @DisplayName("채용공고 검색 테스트")
    void search() {
        // Given
        List<JobopeningEntity> searchResults = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> searchPage = new PageImpl<>(searchResults);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.findByTitleContainingOrContentContaining(
                eq("백엔드"), eq("백엔드"), eq(pageable))).thenReturn(searchPage);

        // When
        Page<JobopeningDTO> result = jobopeningService.search("백엔드", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getTitle().contains("백엔드"));
        verify(jobopeningRepository).findByTitleContainingOrContentContaining("백엔드", "백엔드", pageable);
    }

    @Test
    @DisplayName("조회수 증가 테스트")
    void incrementViewCount() {
        // Given
        when(jobopeningRepository.findById(1L)).thenReturn(Optional.of(testJobEntity));
        when(jobopeningRepository.save(any(JobopeningEntity.class))).thenReturn(testJobEntity);

        // When
        jobopeningService.incrementViewCount(1L);

        // Then
        verify(jobopeningRepository).findById(1L);
        verify(jobopeningRepository).save(any(JobopeningEntity.class));
    }

    @Test
    @DisplayName("활성 채용공고만 조회 테스트")
    void findActiveJobs() {
        // Given
        List<JobopeningEntity> activeJobs = Arrays.asList(testJobEntity);
        Page<JobopeningEntity> activePage = new PageImpl<>(activeJobs);
        Pageable pageable = PageRequest.of(0, 10);

        when(jobopeningRepository.findByIsActiveTrue(pageable)).thenReturn(activePage);

        // When
        Page<JobopeningDTO> result = jobopeningService.findActiveJobs(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getIsActive());
        verify(jobopeningRepository).findByIsActiveTrue(pageable);
    }
}