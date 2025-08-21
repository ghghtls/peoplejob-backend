package com.people.job.board.service;

import com.people.job.board.dto.BoardDTO;
import com.people.job.board.entity.BoardEntity;
import com.people.job.board.repository.BoardRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("게시판 서비스 테스트")
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardServiceImpl boardService;

    private BoardEntity testBoardEntity;
    private BoardDTO testBoardDTO;

    @BeforeEach
    void setUp() {
        testBoardEntity = BoardEntity.builder()
                .boardNo(1L)
                .category("공지사항")
                .title("테스트 게시글")
                .content("테스트 내용")
                .writer("testuser")
                .regdate(LocalDate.now())
                .viewCount(0)
                .build();

        testBoardDTO = BoardDTO.builder()
                .boardNo(1L)
                .category("공지사항")
                .title("테스트 게시글")
                .content("테스트 내용")
                .writer("testuser")
                .regdate(LocalDate.now())
                .viewCount(0)
                .build();
    }

    @Test
    @DisplayName("모든 게시글 조회 테스트")
    void findAll() {
        // Given
        List<BoardEntity> boardList = Arrays.asList(testBoardEntity);
        Page<BoardEntity> boardPage = new PageImpl<>(boardList);
        Pageable pageable = PageRequest.of(0, 10);

        when(boardRepository.findAll(pageable)).thenReturn(boardPage);

        // When
        Page<BoardDTO> result = boardService.findAll(null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("테스트 게시글", result.getContent().get(0).getTitle());
        verify(boardRepository).findAll(pageable);
    }

    @Test
    @DisplayName("게시글 ID로 조회 테스트")
    void findById() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoardEntity));

        // When
        BoardDTO result = boardService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals("테스트 게시글", result.getTitle());
        assertEquals("테스트 내용", result.getContent());
        verify(boardRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외 발생 테스트")
    void findByIdNotFound() {
        // Given
        when(boardRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> boardService.findById(999L));
        verify(boardRepository).findById(999L);
    }

    @Test
    @DisplayName("게시글 저장 테스트")
    void save() {
        // Given
        when(boardRepository.save(any(BoardEntity.class))).thenReturn(testBoardEntity);

        // When
        BoardDTO result = boardService.save(testBoardDTO);

        // Then
        assertNotNull(result);
        assertEquals("테스트 게시글", result.getTitle());
        verify(boardRepository).save(any(BoardEntity.class));
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void update() {
        // Given
        BoardEntity updatedEntity = BoardEntity.builder()
                .boardNo(1L)
                .category("공지사항")
                .title("수정된 제목")
                .content("수정된 내용")
                .writer("testuser")
                .regdate(LocalDate.now())
                .viewCount(5)
                .build();

        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoardEntity));
        when(boardRepository.save(any(BoardEntity.class))).thenReturn(updatedEntity);

        BoardDTO updateDTO = BoardDTO.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        // When
        BoardDTO result = boardService.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("수정된 제목", result.getTitle());
        assertEquals("수정된 내용", result.getContent());
        verify(boardRepository).findById(1L);
        verify(boardRepository).save(any(BoardEntity.class));
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void delete() {
        // Given
        when(boardRepository.existsById(1L)).thenReturn(true);

        // When
        boardService.delete(1L);

        // Then
        verify(boardRepository).existsById(1L);
        verify(boardRepository).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생 테스트")
    void deleteNotFound() {
        // Given
        when(boardRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> boardService.delete(999L));
        verify(boardRepository).existsById(999L);
        verify(boardRepository, never()).deleteById(999L);
    }

    @Test
    @DisplayName("카테고리별 게시글 조회 테스트")
    void findByCategory() {
        // Given
        List<BoardEntity> boardList = Arrays.asList(testBoardEntity);
        Page<BoardEntity> boardPage = new PageImpl<>(boardList);
        Pageable pageable = PageRequest.of(0, 10);

        when(boardRepository.findByCategory(eq("공지사항"), eq(pageable))).thenReturn(boardPage);

        // When
        Page<BoardDTO> result = boardService.findByCategory("공지사항", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("공지사항", result.getContent().get(0).getCategory());
        verify(boardRepository).findByCategory("공지사항", pageable);
    }

    @Test
    @DisplayName("제목 검색 테스트")
    void search() {
        // Given
        List<BoardEntity> searchResults = Arrays.asList(testBoardEntity);
        Page<BoardEntity> searchPage = new PageImpl<>(searchResults);
        Pageable pageable = PageRequest.of(0, 10);

        when(boardRepository.findByTitleContainingOrContentContaining(
                eq("테스트"), eq("테스트"), eq(pageable))).thenReturn(searchPage);

        // When
        Page<BoardDTO> result = boardService.search("테스트", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getTitle().contains("테스트"));
        verify(boardRepository).findByTitleContainingOrContentContaining("테스트", "테스트", pageable);
    }

    @Test
    @DisplayName("조회수 증가 테스트")
    void incrementViewCount() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoardEntity));
        when(boardRepository.save(any(BoardEntity.class))).thenReturn(testBoardEntity);

        // When
        boardService.incrementViewCount(1L);

        // Then
        verify(boardRepository).findById(1L);
        verify(boardRepository).save(any(BoardEntity.class));
    }
}