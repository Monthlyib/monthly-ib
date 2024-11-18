package com.monthlyib.server.api.board.controller;

import com.monthlyib.server.api.board.dto.*;
import com.monthlyib.server.domain.board.service.BoardService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class BoardApiController implements BoardApiControllerIfs{


    private final BoardService boardService;


    @Override
    @GetMapping("/open-api/board")
    public ResponseEntity<PageResponseDto<?>> getBoardList(int page,BoardSearchDto requestDto) {
        Page<BoardSimpleResponseDto> response = boardService.findAllBoardLis(page, requestDto);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @GetMapping("/api/board")
    public ResponseEntity<PageResponseDto<?>> getBoardListForUser(int page, BoardSearchDto requestDto, User user) {
        Page<BoardSimpleResponseDto> response = boardService.findAllBoardListForUser(page, requestDto, user.getUserId());
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));    }

    @Override
    @GetMapping("/open-api/board/{boardId}")
    public ResponseEntity<ResponseDto<?>> getBoard(int replyPage,Long boardId) {
        BoardResponseDto response = boardService.findBoard(replyPage, boardId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/board")
    public ResponseEntity<ResponseDto<?>> postBoard(BoardPostDto requestDto, User user) {
        BoardResponseDto response = boardService.createBoard(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/board/{boardId}")
    public ResponseEntity<ResponseDto<?>> patchBoard(Long boardId, BoardPatchDto requestDto, User user) {
        // 보드 response
        BoardResponseDto response = boardService.updateBoard(requestDto, boardId, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/board/board-file/{boardId}")
    public ResponseEntity<ResponseDto<?>> postBoardFile(Long boardId, MultipartFile[] multipartFile, User user) {
        BoardResponseDto response = boardService.createOrUpdateBoardFile(boardId, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/board/reply/{boardId}")
    public ResponseEntity<ResponseDto<?>> postBoardReply(Long boardId, BoardReplyPostDto requestDto, User user) {
        BoardResponseDto response = boardService.createBoardReply(requestDto, boardId, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/board/reply/{boardReplyId}")
    public ResponseEntity<ResponseDto<?>> patchBoardReply(Long boardReplyId, BoardReplyPatchDto requestDto, User user) {
        BoardResponseDto response = boardService.updateBoardReply(requestDto, boardReplyId, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/board/{boardId}")
    public ResponseEntity<ResponseDto<?>> deleteBoard(Long boardId, User user) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @DeleteMapping("/api/board/reply/{boardReplyId}")
    public ResponseEntity<ResponseDto<?>> deleteBoardReply(Long boardReplyId, User user) {
        boardService.deleteBoardReply(boardReplyId);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @PostMapping("/api/board/reply-vote/{boardReplyId}")
    public ResponseEntity<ResponseDto<?>> voteBoardReply(Long boardReplyId, User user) {
        boardService.voteUser(boardReplyId, user.getUserId());
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }
}
