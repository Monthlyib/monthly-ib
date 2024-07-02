package com.monthlyib.server.domain.board.repository;

import com.monthlyib.server.api.board.dto.BoardSearchDto;
import com.monthlyib.server.api.board.dto.BoardSimpleResponseDto;
import com.monthlyib.server.domain.board.entity.Board;
import com.monthlyib.server.domain.board.entity.BoardFile;
import com.monthlyib.server.domain.board.entity.BoardReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BoardRepository {

    Optional<Board> findBoard(Long boardId);

    Page<BoardSimpleResponseDto> findAllBoards(Pageable pageable, BoardSearchDto searchDto);
    Page<BoardSimpleResponseDto> findAllBoardsByUserId(Pageable pageable, BoardSearchDto searchDto, Long userId);

    Optional<BoardFile> findBoardFile(Long boardFileId);

    List<BoardFile> findAllBoardFile(Long boardId);

    Optional<BoardReply> findBoardReply(Long replyId);

    Page<BoardReply> findAllBoardReply(Pageable pageable, Long boardId);

    Board saveBoard(Board board);

    BoardFile saveBoardFile(BoardFile boardFile);

    BoardReply saveBoardReply(BoardReply boardReply);

    void deleteBoardReply(Long replyId);

    void deleteBoardFile(Long boardFileId);

    void deleteBoard(Long boardId);

}
