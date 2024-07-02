package com.monthlyib.server.domain.board.repository;

import com.monthlyib.server.domain.board.entity.BoardFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardFileJpaRepository extends JpaRepository<BoardFile, Long> {

    Long countByBoardId(Long boardId);

    List<BoardFile> findAllByBoardId(Long boardId);

    void deleteAllByBoardId(Long boardId);
}
