package com.people.job.peoplejob_backend.board.service;

import com.people.job.board.dto.BoardDTO;
import com.people.job.board.entity.BoardEntity;
import com.people.job.board.repository.BoardRepository;
import com.people.job.board.service.BoardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.timeout;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("게시판 서비스 테스트")
class BoardServiceTest {

    @MockitoBean
    private BoardRepository boardRepository;

    @Autowired
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
                .filename(null)
                .originalFilename(null)
                .build();

        testBoardDTO = BoardDTO.builder()
                .boardNo(1L)
                .category("공지사항")
                .title("테스트 게시글")
                .content("테스트 내용")
                .writer("testuser")
                .regdate(LocalDate.now())
                .viewCount(0)
                .filename(null)
                .originalFilename(null)
                .build();
    }

    @Test
    @DisplayName("게시글 등록 테스트")
    void insertBoard() {
        // Given
        when(boardRepository.save(any(BoardEntity.class))).thenReturn(testBoardEntity);

        // When
        assertDoesNotThrow(() -> boardService.insertBoard(testBoardDTO)); // 실제 메서드명과 일치

        // Then
        verify(boardRepository).save(any(BoardEntity.class));
    }

    @Test
    @DisplayName("모든 게시글 조회 테스트")
    void getAllBoards() {
        // Given
        List<BoardEntity> boardList = Arrays.asList(testBoardEntity);
        when(boardRepository.findAll()).thenReturn(boardList);

        // When
        List<BoardDTO> result = boardService.getAllBoards(); // 실제 메서드명과 일치

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("테스트 게시글", result.get(0).getTitle());
        verify(boardRepository).findAll();
    }

    @Test
    @DisplayName("카테고리별 게시글 조회 테스트")
    void getBoardsByCategory() {
        // Given
        List<BoardEntity> boardList = Arrays.asList(testBoardEntity);
        when(boardRepository.findByCategory("공지사항")).thenReturn(boardList);

        // When
        List<BoardDTO> result = boardService.getBoardsByCategory("공지사항"); // 실제 메서드명과 일치

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("공지사항", result.get(0).getCategory());
        verify(boardRepository).findByCategory("공지사항");
    }

    @Test
    @DisplayName("게시글 ID로 조회 테스트")
    void getBoard() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoardEntity));

        // When
        BoardDTO result = boardService.getBoard(1L); // 실제 메서드명과 일치

        // Then
        assertNotNull(result);
        assertEquals("테스트 게시글", result.getTitle());
        assertEquals("테스트 내용", result.getContent());
        verify(boardRepository).findById(1L);
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void updateBoard() {
        // Given
        BoardEntity updatedEntity = BoardEntity.builder()
                .boardNo(1L)
                .category("공지사항")
                .title("수정된 제목")
                .content("수정된 내용")
                .writer("testuser")
                .regdate(LocalDate.now())
                .viewCount(5)
                .filename(null)
                .originalFilename(null)
                .build();

        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoardEntity));
        when(boardRepository.save(any(BoardEntity.class))).thenReturn(updatedEntity);

        BoardDTO updateDTO = BoardDTO.builder()
                .boardNo(1L)
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        // When
        assertDoesNotThrow(() -> boardService.updateBoard(updateDTO)); // 실제 메서드명과 일치

        // Then
        verify(boardRepository).findById(1L);
        verify(boardRepository).save(any(BoardEntity.class));
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deleteBoard() {
        // Given
        doNothing().when(boardRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> boardService.deleteBoard(1L)); // 실제 메서드명과 일치

        // Then
        verify(boardRepository).deleteById(1L);
    }

    @Test
    @DisplayName("조회수 증가 테스트")
    void increaseViewCount() {
        // Given
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoardEntity));
        when(boardRepository.save(any(BoardEntity.class))).thenReturn(testBoardEntity);

        // When
        assertDoesNotThrow(() -> boardService.increaseViewCount(1L)); // 실제 메서드명과 일치

        // Then - @Async method runs in background thread; use timeout
        verify(boardRepository, timeout(5000)).findById(1L);
        verify(boardRepository, timeout(5000)).save(any(BoardEntity.class));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외 발생 테스트")
    void getBoardNotFound() {
        // Given
        when(boardRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> boardService.getBoard(999L));
        verify(boardRepository).findById(999L);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생 테스트")
    void deleteBoardNotFound() {
        // Given
        doThrow(new RuntimeException("게시글을 찾을 수 없습니다."))
                .when(boardRepository).deleteById(999L);

        // When & Then
        assertThrows(RuntimeException.class, () -> boardService.deleteBoard(999L));
        verify(boardRepository).deleteById(999L);
    }
}