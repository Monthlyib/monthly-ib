package com.monthlyib.server.domain.board.repository;

import com.monthlyib.server.api.board.dto.BoardSearchDto;
import com.monthlyib.server.api.board.dto.BoardSimpleResponseDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.board.entity.Board;
import com.monthlyib.server.domain.board.entity.BoardFile;
import com.monthlyib.server.domain.board.entity.BoardReply;
import com.monthlyib.server.domain.board.entity.QBoard;
import com.monthlyib.server.exception.ServiceLogicException;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BoardRepositoryImpl extends QuerydslRepositorySupport implements BoardRepository{

    private final BoardJpaRepository boardJpaRepository;

    private final BoardReplyJpaRepository boardReplyJpaRepository;

    private final BoardFileJpaRepository boardFileJpaRepository;

    public BoardRepositoryImpl(
            BoardJpaRepository boardJpaRepository,
            BoardReplyJpaRepository boardReplyJpaRepository,
            BoardFileJpaRepository boardFileJpaRepository
    ) {
        super(Board.class);
        this.boardJpaRepository = boardJpaRepository;
        this.boardReplyJpaRepository = boardReplyJpaRepository;
        this.boardFileJpaRepository = boardFileJpaRepository;
    }

    QBoard board = QBoard.board;

    @Override
    public Optional<Board> findBoard(Long boardId) {
        return boardJpaRepository.findById(boardId);
    }

    @Override
    public Page<BoardSimpleResponseDto> findAllBoards(Pageable pageable, BoardSearchDto searchDto) {
        JPQLQuery<BoardSimpleResponseDto> query = getBoardSimpleQuery();
        String keyWord = searchDto.getKeyWord();
        if (keyWord != null && !keyWord.isEmpty()) {
            query.where(board.title.containsIgnoreCase(keyWord)
                    .or(board.content.containsIgnoreCase(keyWord))
            );
        }
        List<BoardSimpleResponseDto> list = Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applyPagination(pageable, query)
                .fetch();
        applyBoardCounts(list);
        return new PageImpl<>(list, pageable, query.fetchCount());
    }

    @Override
    public Page<BoardSimpleResponseDto> findAllBoardsByUserId(Pageable pageable, BoardSearchDto searchDto, Long userId) {
        JPQLQuery<BoardSimpleResponseDto> query = getBoardSimpleQuery();
        String keyWord = searchDto.getKeyWord();
        if (keyWord != null && !keyWord.isEmpty()) {
            query.where(board.title.containsIgnoreCase(keyWord)
                    .or(board.content.containsIgnoreCase(keyWord))
            );
        }

        if (userId != null) {
            query.where(board.authorId.eq(userId));
        }
        List<BoardSimpleResponseDto> list = Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applyPagination(pageable, query)
                .fetch();
        applyBoardCounts(list);
        return new PageImpl<>(list, pageable, query.fetchCount());
    }

    @Override
    public Optional<BoardFile> findBoardFile(Long boardFileId) {
        return boardFileJpaRepository.findById(boardFileId);
    }

    @Override
    public List<BoardFile> findAllBoardFile(Long boardId) {
        return boardFileJpaRepository.findAllByBoardId(boardId);
    }

    @Override
    public Optional<BoardReply> findBoardReply(Long replyId) {
        return boardReplyJpaRepository.findById(replyId);
    }

    @Override
    public Page<BoardReply> findAllBoardReply(Pageable pageable, Long boardId) {
        return boardReplyJpaRepository.findAllByBoardId(boardId, pageable);
    }

    @Override
    public Board saveBoard(Board board) {
        return boardJpaRepository.save(board);
    }

    @Override
    public BoardFile saveBoardFile(BoardFile boardFile) {
        return boardFileJpaRepository.save(boardFile);
    }

    @Override
    public BoardReply saveBoardReply(BoardReply boardReply) {
        return boardReplyJpaRepository.save(boardReply);
    }

    @Override
    public void deleteBoardReply(Long replyId) {
        boardReplyJpaRepository.deleteById(replyId);
    }

    @Override
    public void deleteBoardFile(Long boardFileId) {
        boardFileJpaRepository.deleteById(boardFileId);
    }

    @Override
    public void deleteBoard(Long boardId) {
        boardReplyJpaRepository.deleteAllByBoardId(boardId);
        boardFileJpaRepository.deleteAllByBoardId(boardId);
        boardJpaRepository.deleteById(boardId);
    }

    private JPQLQuery<BoardSimpleResponseDto> getBoardSimpleQuery() {

        return from(board)
                .select(
                        Projections.constructor(
                                BoardSimpleResponseDto.class,
                                board.boardId,
                                board.authorId,
                                board.authorUsername,
                                board.authorNickName,
                                board.title,
                                board.content,
                                board.viewCount,
                                board.createAt,
                                board.updateAt
                        )
                );
    }

    private void applyBoardCounts(List<BoardSimpleResponseDto> boards) {
        List<Long> boardIds = boards.stream()
                .map(BoardSimpleResponseDto::getBoardId)
                .toList();
        if (boardIds.isEmpty()) {
            return;
        }

        Map<Long, Long> fileCounts = toCountMap(boardFileJpaRepository.countByBoardIdInGroupByBoardId(boardIds));
        Map<Long, Long> replyCounts = toCountMap(boardReplyJpaRepository.countByBoardIdInGroupByBoardId(boardIds));

        boards.forEach(board -> {
            Long boardId = board.getBoardId();
            board.setFileCount(fileCounts.getOrDefault(boardId, 0L));
            board.setReplyCount(replyCounts.getOrDefault(boardId, 0L));
        });
    }

    private Map<Long, Long> toCountMap(List<BoardCountProjection> counts) {
        return counts.stream()
                .collect(Collectors.toMap(
                        BoardCountProjection::getBoardId,
                        BoardCountProjection::getCount,
                        (left, right) -> left
                ));
    }
}
