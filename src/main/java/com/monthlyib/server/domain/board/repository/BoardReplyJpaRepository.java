package com.monthlyib.server.domain.board.repository;

import com.monthlyib.server.domain.board.entity.BoardReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardReplyJpaRepository extends JpaRepository<BoardReply, Long> {

    Long countByBoardId(Long boardId);

    Page<BoardReply> findAllByBoardId(Long boardId, Pageable pageable);

    void deleteAllByBoardId(Long boardId);
}
