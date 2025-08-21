
import com.fasterxml.jackson.databind.ObjectMapper;
import com.people.job.board.dto.BoardDTO;
import com.people.job.board.service.BoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("게시판 컨트롤러 테스트")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    private BoardDTO testBoard;

    @BeforeEach
    void setUp() {
        testBoard = BoardDTO.builder()
                .boardNo(1L)
                .category("공지사항")
                .title("테스트 게시글")
                .content("테스트 내용입니다.")
                .writer("testuser")
                .regdate(LocalDate.now())
                .viewCount(0)
                .build();
    }

    @Test
    @DisplayName("게시글 목록 조회 테스트")
    void getBoardList() throws Exception {
        // Given
        List<BoardDTO> boardList = Arrays.asList(testBoard);
        Page<BoardDTO> boardPage = new PageImpl<>(boardList, PageRequest.of(0, 10), 1);

        when(boardService.findAll(any(), any())).thenReturn(boardPage);

        // When & Then
        mockMvc.perform(get("/api/boards")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("테스트 게시글"));
    }

    @Test
    @DisplayName("게시글 상세 조회 테스트")
    void getBoardDetail() throws Exception {
        // Given
        when(boardService.findById(1L)).thenReturn(testBoard);

        // When & Then
        mockMvc.perform(get("/api/boards/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 게시글"))
                .andExpect(jsonPath("$.content").value("테스트 내용입니다."));
    }

    @Test
    @DisplayName("게시글 등록 테스트")
    void createBoard() throws Exception {
        // Given
        BoardDTO newBoard = BoardDTO.builder()
                .category("자유게시판")
                .title("새로운 게시글")
                .content("새로운 내용")
                .writer("testuser")
                .build();

        when(boardService.save(any(BoardDTO.class))).thenReturn(testBoard);

        // When & Then
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBoard)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("테스트 게시글"));
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void updateBoard() throws Exception {
        // Given
        BoardDTO updatedBoard = BoardDTO.builder()
                .boardNo(1L)
                .category("공지사항")
                .title("수정된 제목")
                .content("수정된 내용")
                .writer("testuser")
                .regdate(LocalDate.now())
                .viewCount(5)
                .build();

        when(boardService.update(eq(1L), any(BoardDTO.class))).thenReturn(updatedBoard);

        // When & Then
        mockMvc.perform(put("/api/boards/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBoard)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"));
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deleteBoard() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/boards/{id}", 1L))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("파일 업로드와 함께 게시글 등록 테스트")
    void createBoardWithFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());

        MockMultipartFile boardData = new MockMultipartFile(
                "board", "", "application/json",
                objectMapper.writeValueAsString(testBoard).getBytes());

        when(boardService.saveWithFile(any(BoardDTO.class), any())).thenReturn(testBoard);

        // When & Then
        mockMvc.perform(multipart("/api/boards/upload")
                        .file(file)
                        .file(boardData))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("카테고리별 게시글 조회 테스트")
    void getBoardsByCategory() throws Exception {
        // Given
        List<BoardDTO> boardList = Arrays.asList(testBoard);
        Page<BoardDTO> boardPage = new PageImpl<>(boardList, PageRequest.of(0, 10), 1);

        when(boardService.findByCategory(eq("공지사항"), any())).thenReturn(boardPage);

        // When & Then
        mockMvc.perform(get("/api/boards/category/{category}", "공지사항")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].category").value("공지사항"));
    }

    @Test
    @DisplayName("게시글 검색 테스트")
    void searchBoards() throws Exception {
        // Given
        List<BoardDTO> searchResults = Arrays.asList(testBoard);
        Page<BoardDTO> searchPage = new PageImpl<>(searchResults, PageRequest.of(0, 10), 1);

        when(boardService.search(eq("테스트"), any())).thenReturn(searchPage);

        // When & Then
        mockMvc.perform(get("/api/boards/search")
                        .param("keyword", "테스트")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("테스트 게시글"));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 404 에러 테스트")
    void getBoardNotFound() throws Exception {
        // Given
        when(boardService.findById(999L))
                .thenThrow(new RuntimeException("게시글을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/boards/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("잘못된 데이터로 게시글 등록 시 400 에러 테스트")
    void createBoardWithInvalidData() throws Exception {
        // Given
        BoardDTO invalidBoard = BoardDTO.builder()
                .title("") // 빈 제목
                .content("내용")
                .build();

        // When & Then
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBoard)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}