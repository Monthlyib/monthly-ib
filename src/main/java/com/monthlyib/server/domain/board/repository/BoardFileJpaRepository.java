package com.monthlyib.server.domain.board.repository;

import com.monthlyib.server.domain.board.entity.BoardFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BoardFileJpaRepository extends JpaRepository<BoardFile, Long> {

    Long countByBoardId(Long boardId);

    @Query("""
            select bf.boardId as boardId, count(bf) as count
            from BoardFile bf
            where bf.boardId in :boardIds
            group by bf.boardId
            """)
    List<BoardCountProjection> countByBoardIdInGroupByBoardId(@Param("boardIds") Collection<Long> boardIds);

    List<BoardFile> findAllByBoardId(Long boardId);

    void deleteAllByBoardId(Long boardId);
}
