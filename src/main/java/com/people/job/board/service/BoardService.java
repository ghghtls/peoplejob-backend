package com.people.job.board.service;

import com.people.job.board.dto.BoardDTO;

import java.util.List;

public interface BoardService {

    void insertBoard(BoardDTO dto);

    List<BoardDTO> getAllBoards();

    List<BoardDTO> getBoardsByCategory(String category);

    BoardDTO getBoard(Long boardNo);

    void updateBoard(BoardDTO dto);

    void deleteBoard(Long boardNo);

    void increaseViewCount(Long boardNo);

    List<BoardDTO> searchBoards(String keyword);
}
