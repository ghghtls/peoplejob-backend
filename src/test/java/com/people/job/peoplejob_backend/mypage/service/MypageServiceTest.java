package com.people.job.peoplejob_backend.mypage.service;

import com.people.job.apply.dto.ApplyDTO;
import com.people.job.apply.entity.ApplyEntity;
import com.people.job.apply.repository.ApplyRepository;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.mypage.service.MypageServiceImpl;
import com.people.job.resume.dto.ResumeDTO;
import com.people.job.resume.entity.ResumeEntity;
import com.people.job.resume.repository.ResumeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("마이페이지 서비스 테스트")
class MypageServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private JobopeningRepository jobopeningRepository;

    @InjectMocks
    private MypageServiceImpl mypageService;

    @Test
    @DisplayName("내 이력서 목록 조회")
    void getMyResumes() {
        ResumeEntity entity = ResumeEntity.builder()
                .resumeNo(1L)
                .title("백엔드 개발자 이력서")
                .content("성실하고 책임감이 강합니다.")
                .education("대졸")
                .career("1년")
                .hopeJobtype("백엔드")
                .hopeLocation("서울")
                .workType("정규직")
                .regdate(LocalDate.now())
                .userNo(1L)
                .build();

        when(resumeRepository.findByUserNo(1L)).thenReturn(List.of(entity));

        List<ResumeDTO> result = mypageService.getMyResumes(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getResumeNo());
        assertEquals("백엔드 개발자 이력서", result.get(0).getTitle());
        verify(resumeRepository).findByUserNo(1L);
    }

    @Test
    @DisplayName("내 이력서 목록 조회 - 결과 없음")
    void getMyResumes_empty() {
        when(resumeRepository.findByUserNo(99L)).thenReturn(List.of());

        List<ResumeDTO> result = mypageService.getMyResumes(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(resumeRepository).findByUserNo(99L);
    }

    @Test
    @DisplayName("내 지원 목록 조회")
    void getMyApplies() {
        ApplyEntity entity = ApplyEntity.builder()
                .applyNo(1L)
                .jobNo(1L)
                .userNo(1L)
                .resumeNo(1L)
                .applyDate(LocalDate.now())
                .status("PENDING")
                .message("잘 부탁드립니다.")
                .build();

        when(applyRepository.findByUserNoOrderByApplyDateDesc(1L)).thenReturn(List.of(entity));

        List<ApplyDTO> result = mypageService.getMyApplies(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getApplyNo());
        assertEquals("PENDING", result.get(0).getStatus());
        verify(applyRepository).findByUserNoOrderByApplyDateDesc(1L);
    }

    @Test
    @DisplayName("내 지원 목록 조회 - 결과 없음")
    void getMyApplies_empty() {
        when(applyRepository.findByUserNoOrderByApplyDateDesc(99L)).thenReturn(List.of());

        List<ApplyDTO> result = mypageService.getMyApplies(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(applyRepository).findByUserNoOrderByApplyDateDesc(99L);
    }

    @Test
    @DisplayName("회사의 내 채용공고 목록 조회")
    void getMyJobopenings() {
        JobopeningEntity entity = JobopeningEntity.builder()
                .jobNo(1L)
                .title("백엔드 개발자 채용")
                .company("피플잡")
                .userNo(1L)
                .regdate(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(jobopeningRepository.findByUserNo(1L)).thenReturn(List.of(entity));

        List<JobopeningDTO> result = mypageService.getMyJobopenings(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getJobNo());
        assertEquals("백엔드 개발자 채용", result.get(0).getTitle());
        verify(jobopeningRepository).findByUserNo(1L);
    }

    @Test
    @DisplayName("채용공고의 지원자 목록 조회")
    void getAppliesForMyJob() {
        ApplyEntity entity = ApplyEntity.builder()
                .applyNo(1L)
                .jobNo(2L)
                .userNo(3L)
                .resumeNo(1L)
                .applyDate(LocalDate.now())
                .status("PENDING")
                .build();

        when(applyRepository.findByJobNo(2L)).thenReturn(List.of(entity));

        List<ApplyDTO> result = mypageService.getAppliesForMyJob(2L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getUserNo());
        assertEquals(2L, result.get(0).getJobNo());
        verify(applyRepository).findByJobNo(2L);
    }

    @Test
    @DisplayName("회사의 내 채용공고 목록 조회 - 결과 없음")
    void getMyJobopenings_empty() {
        when(jobopeningRepository.findByUserNo(99L)).thenReturn(List.of());

        List<JobopeningDTO> result = mypageService.getMyJobopenings(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jobopeningRepository).findByUserNo(99L);
    }
}
