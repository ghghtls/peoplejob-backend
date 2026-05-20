package com.people.job.peoplejob_backend.inquiry.service;

import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.inquiry.entity.InquiryEntity;
import com.people.job.inquiry.repository.InquiryRepository;
import com.people.job.inquiry.service.InquiryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("문의 서비스 테스트")
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private InquiryServiceImpl inquiryService;

    private InquiryEntity testEntity;
    private InquiryDTO testDTO;

    @BeforeEach
    void setUp() {
        testEntity = InquiryEntity.builder()
                .inquiryNo(1L)
                .title("서비스 문의")
                .content("채용공고 등록 방법이 궁금합니다.")
                .writer("홍길동")
                .email("hong@test.com")
                .phone("010-1234-5678")
                .category("일반문의")
                .regdate(LocalDate.now())
                .isAnswered(false)
                .build();

        testDTO = InquiryDTO.builder()
                .inquiryNo(1L)
                .title("서비스 문의")
                .content("채용공고 등록 방법이 궁금합니다.")
                .writer("홍길동")
                .email("hong@test.com")
                .phone("010-1234-5678")
                .category("일반문의")
                .regdate(LocalDate.now())
                .isAnswered(false)
                .build();
    }

    @Test
    @DisplayName("문의 등록")
    void insertInquiry() {
        when(inquiryRepository.save(any(InquiryEntity.class))).thenReturn(testEntity);

        assertDoesNotThrow(() -> inquiryService.insertInquiry(testDTO));

        verify(inquiryRepository).save(any(InquiryEntity.class));
    }

    @Test
    @DisplayName("전체 문의 조회")
    void getAllInquiries() {
        when(inquiryRepository.findAll()).thenReturn(List.of(testEntity));

        List<InquiryDTO> result = inquiryService.getAllInquiries();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("서비스 문의", result.get(0).getTitle());
        assertEquals("hong@test.com", result.get(0).getEmail());
        verify(inquiryRepository).findAll();
    }

    @Test
    @DisplayName("문의 단건 조회 성공")
    void getInquiry_success() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testEntity));

        InquiryDTO result = inquiryService.getInquiry(1L);

        assertNotNull(result);
        assertEquals(1L, result.getInquiryNo());
        assertEquals("서비스 문의", result.getTitle());
        assertFalse(result.getIsAnswered());
        verify(inquiryRepository).findById(1L);
    }

    @Test
    @DisplayName("문의 단건 조회 실패 - 존재하지 않음")
    void getInquiry_notFound_throws() {
        when(inquiryRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inquiryService.getInquiry(999L));
        assertTrue(ex.getMessage().contains("문의가 존재하지 않습니다."));
        verify(inquiryRepository).findById(999L);
    }

    @Test
    @DisplayName("문의 수정 성공")
    void updateInquiry_success() {
        InquiryDTO updateDTO = InquiryDTO.builder()
                .inquiryNo(1L)
                .title("수정된 문의 제목")
                .content("수정된 내용입니다.")
                .build();

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(inquiryRepository.save(any(InquiryEntity.class))).thenReturn(testEntity);

        assertDoesNotThrow(() -> inquiryService.updateInquiry(updateDTO));

        verify(inquiryRepository).findById(1L);
        verify(inquiryRepository).save(any(InquiryEntity.class));
    }

    @Test
    @DisplayName("문의 수정 실패 - 존재하지 않음")
    void updateInquiry_notFound_throws() {
        InquiryDTO updateDTO = InquiryDTO.builder()
                .inquiryNo(999L)
                .title("x")
                .content("x")
                .build();

        when(inquiryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inquiryService.updateInquiry(updateDTO));
        verify(inquiryRepository, never()).save(any());
    }

    @Test
    @DisplayName("문의 삭제")
    void deleteInquiry() {
        doNothing().when(inquiryRepository).deleteById(1L);

        assertDoesNotThrow(() -> inquiryService.deleteInquiry(1L));

        verify(inquiryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("이메일로 문의 목록 조회")
    void getInquiriesByEmail() {
        when(inquiryRepository.findByEmail("hong@test.com")).thenReturn(List.of(testEntity));

        List<InquiryDTO> result = inquiryService.getInquiriesByEmail("hong@test.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("hong@test.com", result.get(0).getEmail());
        verify(inquiryRepository).findByEmail("hong@test.com");
    }

    @Test
    @DisplayName("문의 답변 등록 성공")
    void answerInquiry_success() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(inquiryRepository.save(any(InquiryEntity.class))).thenReturn(testEntity);

        assertDoesNotThrow(() -> inquiryService.answerInquiry(1L, "답변 내용입니다."));

        verify(inquiryRepository).findById(1L);
        verify(inquiryRepository).save(any(InquiryEntity.class));
    }

    @Test
    @DisplayName("문의 답변 등록 실패 - 존재하지 않음")
    void answerInquiry_notFound_throws() {
        when(inquiryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inquiryService.answerInquiry(999L, "답변"));
        verify(inquiryRepository, never()).save(any());
    }
}
