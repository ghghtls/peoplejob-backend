package com.people.job.resume.service;

import com.people.job.resume.dto.ResumeDTO;
import com.people.job.resume.entity.ResumeEntity;
import com.people.job.resume.repository.ResumeRepository;
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
@DisplayName("이력서 서비스 테스트")
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private ResumeEntity testResumeEntity;
    private ResumeDTO testResumeDTO;

    @BeforeEach
    void setUp() {
        testResumeEntity = ResumeEntity.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("백엔드 개발자 이력서")
                .name("홍길동")
                .email("hong@example.com")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .education("컴퓨터공학과 학사")
                .experience("Java/Spring 개발 3년")
                .skills("Java, Spring Boot, MySQL")
                .introduction("백엔드 개발에 열정이 있는 개발자입니다.")
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testResumeDTO = ResumeDTO.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("백엔드 개발자 이력서")
                .name("홍길동")
                .email("hong@example.com")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .education("컴퓨터공학과 학사")
                .experience("Java/Spring 개발 3년")
                .skills("Java, Spring Boot, MySQL")
                .introduction("백엔드 개발에 열정이 있는 개발자입니다.")
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("이력서 저장 테스트")
    void save() {
        // Given
        when(resumeRepository.save(any(ResumeEntity.class))).thenReturn(testResumeEntity);

        // When
        ResumeDTO result = resumeService.save(testResumeDTO);

        // Then
        assertNotNull(result);
        assertEquals("백엔드 개발자 이력서", result.getTitle());
        assertEquals("홍길동", result.getName());
        verify(resumeRepository).save(any(ResumeEntity.class));
    }

    @Test
    @DisplayName("이력서 ID로 조회 테스트")
    void findById() {
        // Given
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(testResumeEntity));

        // When
        ResumeDTO result = resumeService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals("백엔드 개발자 이력서", result.getTitle());
        assertEquals("홍길동", result.getName());
        verify(resumeRepository).findById(1L);
    }

    @Test
    @DisplayName("이력서 수정 테스트")
    void update() {
        // Given
        ResumeEntity updatedEntity = ResumeEntity.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("시니어 백엔드 개발자 이력서")
                .name("홍길동")
                .email("hong@example.com")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .education("컴퓨터공학과 학사")
                .experience("Java/Spring 개발 5년")
                .skills("Java, Spring Boot, MySQL, Redis")
                .introduction("시니어 백엔드 개발자로 성장하고 있습니다.")
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(testResumeEntity));
        when(resumeRepository.save(any(ResumeEntity.class))).thenReturn(updatedEntity);

        ResumeDTO updateDTO = ResumeDTO.builder()
                .title("시니어 백엔드 개발자 이력서")
                .experience("Java/Spring 개발 5년")
                .skills("Java, Spring Boot, MySQL, Redis")
                .introduction("시니어 백엔드 개발자로 성장하고 있습니다.")
                .build();

        // When
        ResumeDTO result = resumeService.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("시니어 백엔드 개발자 이력서", result.getTitle());
        assertEquals("Java/Spring 개발 5년", result.getExperience());
        verify(resumeRepository).findById(1L);
        verify(resumeRepository).save(any(ResumeEntity.class));
    }

    @Test
    @DisplayName("이력서 삭제 테스트")
    void delete() {
        // Given
        when(resumeRepository.existsById(1L)).thenReturn(true);

        // When
        resumeService.delete(1L);

        // Then
        verify(resumeRepository).existsById(1L);
        verify(resumeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("사용자별 이력서 조회 테스트")
    void findByUserNo() {
        // Given
        List<ResumeEntity> resumeList = Arrays.asList(testResumeEntity);
        Page<ResumeEntity> resumePage = new PageImpl<>(resumeList);
        Pageable pageable = PageRequest.of(0, 10);

        when(resumeRepository.findByUserNo(eq(1L), eq(pageable))).thenReturn(resumePage);

        // When
        Page<ResumeDTO> result = resumeService.findByUserNo(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getUserNo());
        verify(resumeRepository).findByUserNo(1L, pageable);
    }

    @Test
    @DisplayName("공개 이력서만 조회 테스트")
    void findPublicResumes() {
        // Given
        List<ResumeEntity> publicResumes = Arrays.asList(testResumeEntity);
        Page<ResumeEntity> resumePage = new PageImpl<>(publicResumes);
        Pageable pageable = PageRequest.of(0, 10);

        when(resumeRepository.findByIsPublicTrue(pageable)).thenReturn(resumePage);

        // When
        Page<ResumeDTO> result = resumeService.findPublicResumes(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getIsPublic());
        verify(resumeRepository).findByIsPublicTrue(pageable);
    }

    @Test
    @DisplayName("이력서 검색 테스트")
    void search() {
        // Given
        List<ResumeEntity> searchResults = Arrays.asList(testResumeEntity);
        Page<ResumeEntity> searchPage = new PageImpl<>(searchResults);
        Pageable pageable = PageRequest.of(0, 10);

        when(resumeRepository.findByTitleContainingOrSkillsContainingOrIntroductionContaining(
                eq("백엔드"), eq("백엔드"), eq("백엔드"), eq(pageable))).thenReturn(searchPage);

        // When
        Page<ResumeDTO> result = resum// When
        Page<ResumeDTO> result = resumeService.search("백엔드", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getTitle().contains("백엔드"));
        verify(resumeRepository).findByTitleContainingOrSkillsContainingOrIntroductionContaining(
                "백엔드", "백엔드", "백엔드", pageable);
    }

    @Test
    @DisplayName("이력서 공개 상태 토글 테스트")
    void togglePublicStatus() {
        // Given
        ResumeEntity privateEntity = ResumeEntity.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("백엔드 개발자 이력서")
                .name("홍길동")
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(testResumeEntity));
        when(resumeRepository.save(any(ResumeEntity.class))).thenReturn(privateEntity);

        // When
        ResumeDTO result = resumeService.togglePublicStatus(1L);

        // Then
        assertNotNull(result);
        assertFalse(result.getIsPublic());
        verify(resumeRepository).findById(1L);
        verify(resumeRepository).save(any(ResumeEntity.class));
    }

    @Test
    @DisplayName("존재하지 않는 이력서 조회 시 예외 발생 테스트")
    void findByIdNotFound() {
        // Given
        when(resumeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> resumeService.findById(999L));
        verify(resumeRepository).findById(999L);
    }

    @Test
    @DisplayName("이력서 제목 검증 테스트")
    void validateResumeTitle() {
        // Given
        ResumeDTO invalidResume = ResumeDTO.builder()
                .userNo(1L)
                .title("") // 빈 제목
                .name("홍길동")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> resumeService.save(invalidResume));
    }

    @Test
    @DisplayName("이력서 이름 검증 테스트")
    void validateResumeName() {
        // Given
        ResumeDTO invalidResume = ResumeDTO.builder()
                .userNo(1L)
                .title("개발자 이력서")
                .name("") // 빈 이름
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> resumeService.save(invalidResume));
    }
}