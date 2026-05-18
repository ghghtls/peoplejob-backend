package com.people.job.board.service;

import com.people.job.board.dto.BoardDTO;
import com.people.job.board.entity.BoardEntity;
import com.people.job.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

    @Override
    public void insertBoard(BoardDTO dto) {
        BoardEntity entity = BoardEntity.builder()
                .category(dto.getCategory())
                .title(dto.getTitle())
                .content(dto.getContent())
                .writer(dto.getWriter())
                .regdate(LocalDate.now())
                .filename(dto.getFilename())
                .originalFilename(dto.getOriginalFilename())
                .viewCount(0)
                .build();

        boardRepository.save(entity);
    }

    @Override
    public List<BoardDTO> getAllBoards() {
        return boardRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BoardDTO> getBoardsByCategory(String category) {
        return boardRepository.findByCategory(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BoardDTO getBoard(Long boardNo) {
        BoardEntity entity = boardRepository.findById(boardNo)
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다."));
        return toDTO(entity);
    }

    @Override
    public void updateBoard(BoardDTO dto) {
        BoardEntity entity = boardRepository.findById(dto.getBoardNo())
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다."));

        entity.setCategory(dto.getCategory());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setFilename(dto.getFilename());
        entity.setOriginalFilename(dto.getOriginalFilename());

        boardRepository.save(entity);
    }

    @Override
    public void deleteBoard(Long boardNo) {
        boardRepository.deleteById(boardNo);
    }

    @Override
    public List<BoardDTO> searchBoards(String keyword) {
        return boardRepository.findByTitleContainingOrContentContaining(keyword, keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void increaseViewCount(Long boardNo) {
        BoardEntity entity = boardRepository.findById(boardNo)
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다."));

        entity.setViewCount(entity.getViewCount() + 1);
        boardRepository.save(entity);
    }

    private BoardDTO toDTO(BoardEntity e) {
        return BoardDTO.builder()
                .boardNo(e.getBoardNo())
                .category(e.getCategory())
                .title(e.getTitle())
                .content(e.getContent())
                .writer(e.getWriter())
                .regdate(e.getRegdate())
                .filename(e.getFilename())
                .originalFilename(e.getOriginalFilename())
                .viewCount(e.getViewCount())
                .build();
    }
}
