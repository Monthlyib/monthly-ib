package com.monthlyib.server.domain.board.repository;

import com.monthlyib.server.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardJpaRepository extends JpaRepository<Board, Long> {
}
