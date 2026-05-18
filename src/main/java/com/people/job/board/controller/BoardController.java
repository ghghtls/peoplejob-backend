package com.people.job.board.controller;

import com.people.job.board.dto.BoardDTO;
import com.people.job.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<?> insert(@RequestBody BoardDTO dto) {
        boardService.insertBoard(dto);
        return ResponseEntity.ok("게시글 등록 완료");
    }

    @GetMapping
    public ResponseEntity<List<BoardDTO>> getAll() {
        return ResponseEntity.ok(boardService.getAllBoards());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<BoardDTO>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(boardService.getBoardsByCategory(category));
    }

    @GetMapping("/{boardNo}")
    public ResponseEntity<BoardDTO> detail(@PathVariable Long boardNo) {
        boardService.increaseViewCount(boardNo);
        return ResponseEntity.ok(boardService.getBoard(boardNo));
    }

    @PutMapping("/{boardNo}")
    public ResponseEntity<?> update(@PathVariable Long boardNo, @RequestBody BoardDTO dto) {
        dto.setBoardNo(boardNo);
        boardService.updateBoard(dto);
        return ResponseEntity.ok("게시글 수정 완료");
    }

    @DeleteMapping("/{boardNo}")
    public ResponseEntity<?> delete(@PathVariable Long boardNo) {
        boardService.deleteBoard(boardNo);
        return ResponseEntity.ok("게시글 삭제 완료");
    }

    @GetMapping("/search")
    public ResponseEntity<List<BoardDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(boardService.searchBoards(keyword));
    }

    @PatchMapping("/{boardNo}/view")
    public ResponseEntity<?> increaseView(@PathVariable Long boardNo) {
        boardService.increaseViewCount(boardNo);
        return ResponseEntity.ok("조회수 증가 완료");
    }
}
