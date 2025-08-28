package com.people.job.peoplejob_backend.resume.service;

import com.people.job.resume.dto.ResumeDTO;
import com.people.job.resume.entity.ResumeEntity;
import com.people.job.resume.repository.ResumeRepository;
import com.people.job.resume.service.ResumeServiceImpl;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("이력서 서비스 테스트")
class ResumeServiceTest {

    @MockitoBean
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeServiceImpl resumeService;

    private ResumeEntity testResumeEntity;
    private ResumeDTO testResumeDTO;

    @BeforeEach
    void setUp() {
        // 실제 DB 스키마와 정확히 일치하도록 수정
        testResumeEntity = ResumeEntity.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("백엔드 개발자 이력서")
                .content("상세한 이력서 내용입니다.")
                .education("컴퓨터공학과 학사")
                .career("Java/Spring 개발 3년") // career 필드
                .certificate("정보처리기사") // certificate 필드
                .hopeJobtype("IT/소프트웨어") // hopeJobtype 필드
                .hopeLocation("서울") // hopeLocation 필드
                .salary("3000만원~4000만원") // salary 필드
                .workType("정규직") // workType 필드
                .regdate(LocalDate.now()) // regdate 필드 (LocalDate)
                .imagePath(null)
                .originalImage(null)
                .build();

        testResumeDTO = ResumeDTO.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("백엔드 개발자 이력서")
                .content("상세한 이력서 내용입니다.")
                .education("컴퓨터공학과 학사")
                .career("Java/Spring 개발 3년")
                .certificate("정보처리기사")
                .hopeJobtype("IT/소프트웨어")
                .hopeLocation("서울")
                .salary("3000만원~4000만원")
                .workType("정규직")
                .regdate(LocalDate.now())
                .imagePath(null)
                .originalImage(null)
                .build();
    }

    @Test
    @DisplayName("이력서 등록 테스트")
    void insertResume() {
        // Given
        when(resumeRepository.save(any(ResumeEntity.class))).thenReturn(testResumeEntity);

        // When
        Long result = resumeService.insertResume(testResumeDTO); // 실제 메서드명과 일치

        // Then
        assertNotNull(result);
        assertEquals(1L, result);
        verify(resumeRepository).save(any(ResumeEntity.class));
    }

    @Test
    @DisplayName("모든 이력서 조회 테스트")
    void selectAll() {
        // Given
        List<ResumeEntity> resumeList = Arrays.asList(testResumeEntity);
        when(resumeRepository.findAll()).thenReturn(resumeList);

        // When
        List<ResumeDTO> result = resumeService.selectAll(); // 실제 메서드명과 일치

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("백엔드 개발자 이력서", result.get(0).getTitle());
        verify(resumeRepository).findAll();
    }

    @Test
    @DisplayName("이력서 ID로 조회 테스트")
    void selectByNo() {
        // Given
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(testResumeEntity));

        // When
        ResumeDTO result = resumeService.selectByNo(1L); // 실제 메서드명과 일치

        // Then
        assertNotNull(result);
        assertEquals("백엔드 개발자 이력서", result.getTitle());
        assertEquals("Java/Spring 개발 3년", result.getCareer());
        verify(resumeRepository).findById(1L);
    }

    @Test
    @DisplayName("이력서 수정 테스트")
    void updateResume() {
        // Given
        ResumeEntity updatedEntity = ResumeEntity.builder()
                .resumeNo(1L)
                .userNo(1L)
                .title("시니어 백엔드 개발자 이력서")
                .content("수정된 이력서 내용입니다.")
                .education("컴퓨터공학과 학사")
                .career("Java/Spring 개발 5년")
                .certificate("정보처리기사, AWS 자격증")
                .hopeJobtype("IT/소프트웨어")
                .hopeLocation("서울")
                .salary("4000만원~5000만원")
                .workType("정규직")
                .regdate(LocalDate.now())
                .imagePath(null)
                .originalImage(null)
                .build();

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(testResumeEntity));
        when(resumeRepository.save(any(ResumeEntity.class))).thenReturn(updatedEntity);

        ResumeDTO updateDTO = ResumeDTO.builder()
                .resumeNo(1L)
                .title("시니어 백엔드 개발자 이력서")
                .content("수정된 이력서 내용입니다.")
                .career("Java/Spring 개발 5년")
                .certificate("정보처리기사, AWS 자격증")
                .salary("4000만원~5000만원")
                .build();

        // When
        assertDoesNotThrow(() -> resumeService.updateResume(updateDTO)); // 실제 메서드명과 일치

        // Then
        verify(resumeRepository).findById(1L);
        verify(resumeRepository).save(any(ResumeEntity.class));
    }

    @Test
    @DisplayName("이력서 삭제 테스트")
    void deleteResume() {
        // Given
        doNothing().when(resumeRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> resumeService.deleteResume(1L)); // 실제 메서드명과 일치

        // Then
        verify(resumeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("사용자별 이력서 조회 테스트")
    void selectByUserNo() {
        // Given
        List<ResumeEntity> resumeList = Arrays.asList(testResumeEntity);
        when(resumeRepository.findByUserNo(1L)).thenReturn(resumeList);

        // When
        List<ResumeDTO> result = resumeService.selectByUserNo(1L); // 실제 메서드명과 일치

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserNo());
        verify(resumeRepository).findByUserNo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 이력서 조회 시 예외 발생 테스트")
    void selectByNoNotFound() {
        // Given
        when(resumeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> resumeService.selectByNo(999L));
        verify(resumeRepository).findById(999L);
    }
}