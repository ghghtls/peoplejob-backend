package com.people.job.board.repository;

import com.people.job.board.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    List<BoardEntity> findByCategory(String category);
}
