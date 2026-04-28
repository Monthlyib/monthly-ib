package com.monthlyib.server.domain.board.repository;

import com.monthlyib.server.domain.board.entity.BoardReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BoardReplyJpaRepository extends JpaRepository<BoardReply, Long> {

    Long countByBoardId(Long boardId);

    @Query("""
            select br.boardId as boardId, count(br) as count
            from BoardReply br
            where br.boardId in :boardIds
            group by br.boardId
            """)
    List<BoardCountProjection> countByBoardIdInGroupByBoardId(@Param("boardIds") Collection<Long> boardIds);

    Page<BoardReply> findAllByBoardId(Long boardId, Pageable pageable);

    void deleteAllByBoardId(Long boardId);
}
