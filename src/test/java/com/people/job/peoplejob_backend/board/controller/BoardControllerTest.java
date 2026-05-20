package com.people.job.peoplejob_backend.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.board.controller.BoardController;
import com.people.job.board.dto.BoardDTO;
import com.people.job.board.service.BoardService;
import com.people.job.user.security.JwtTokenProvider;
import com.people.job.user.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("게시판 컨트롤러 테스트")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardService boardService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private BoardDTO testBoard;

    @BeforeEach
    void setUp() {
        testBoard = BoardDTO.builder()
                .boardNo(1L)
                .category("공지사항")
                .title("테스트 게시글")
                .content("테스트 내용입니다.")
                .writer("관리자")
                .regdate(LocalDate.now())
                .viewCount(0)
                .build();
    }

    @Test
    @DisplayName("게시글 등록 성공 테스트")
    void insertBoardSuccess() throws Exception {
        // Given
        doNothing().when(boardService).insertBoard(any(BoardDTO.class));

        // When & Then
        mockMvc.perform(post("/api/board") // 실제 매핑 경로
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBoard)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("게시글 등록 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("전체 게시글 조회 성공 테스트")
    void getAllBoardsSuccess() throws Exception {
        // Given
        List<BoardDTO> boards = Arrays.asList(testBoard);
        when(boardService.getAllBoards()).thenReturn(boards); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/board"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("테스트 게시글"));
    }

    @Test
    @DisplayName("카테고리별 게시글 조회 성공 테스트")
    void getBoardsByCategorySuccess() throws Exception {
        // Given
        List<BoardDTO> boards = Arrays.asList(testBoard);
        when(boardService.getBoardsByCategory("공지사항")).thenReturn(boards); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/board/category/{category}", "공지사항"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").value("공지사항"));
    }

    @Test
    @DisplayName("게시글 상세 조회 성공 테스트")
    void getBoardDetailSuccess() throws Exception {
        // Given
        when(boardService.getBoard(1L)).thenReturn(testBoard); // 실제 메서드명
        doNothing().when(boardService).increaseViewCount(1L); // 실제 메서드명

        // When & Then
        mockMvc.perform(get("/api/board/{boardNo}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardNo").value(1))
                .andExpect(jsonPath("$.title").value("테스트 게시글"));

        // 조회수 증가 메서드가 호출되었는지 확인
        verify(boardService).increaseViewCount(1L);
    }

    @Test
    @DisplayName("게시글 수정 성공 테스트")
    void updateBoardSuccess() throws Exception {
        // Given
        BoardDTO updateBoard = BoardDTO.builder()
                .boardNo(1L)
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        doNothing().when(boardService).updateBoard(any(BoardDTO.class));

        // When & Then
        mockMvc.perform(put("/api/board/{boardNo}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBoard)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("게시글 수정 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("게시글 삭제 성공 테스트")
    void deleteBoardSuccess() throws Exception {
        // Given
        doNothing().when(boardService).deleteBoard(1L);

        // When & Then
        mockMvc.perform(delete("/api/board/{boardNo}", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("게시글 삭제 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("게시글 등록 실패 테스트 - 필수 정보 누락")
    void insertBoardFailMissingInfo() throws Exception {
        // Given
        BoardDTO invalidBoard = BoardDTO.builder()
                .title("") // 빈 제목
                .content("내용")
                .build();

        doThrow(new RuntimeException("제목은 필수입니다."))
                .when(boardService).insertBoard(any(BoardDTO.class));

        // When & Then
        mockMvc.perform(post("/api/board")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBoard)))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // RuntimeException으로 500 에러
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 테스트")
    void getBoardDetailNotFound() throws Exception {
        // Given
        when(boardService.getBoard(999L))
                .thenThrow(new RuntimeException("게시글을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/board/{boardNo}", 999L))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // RuntimeException으로 500 에러
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 테스트")
    void updateBoardNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("게시글을 찾을 수 없습니다."))
                .when(boardService).updateBoard(any(BoardDTO.class));

        // When & Then
        mockMvc.perform(put("/api/board/{boardNo}", 999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBoard)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 테스트")
    void deleteBoardNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("게시글을 찾을 수 없습니다."))
                .when(boardService).deleteBoard(999L);

        // When & Then
        mockMvc.perform(delete("/api/board/{boardNo}", 999L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}