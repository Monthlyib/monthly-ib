package com.monthlyib.server.domain.board.service;


import com.monthlyib.server.api.board.dto.*;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.board.entity.Board;
import com.monthlyib.server.domain.board.entity.BoardFile;
import com.monthlyib.server.domain.board.entity.BoardReply;
import com.monthlyib.server.domain.board.repository.BoardRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.service.UserService;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;

    private final FileService fileService;

    private final UserService userService;

    public Page<BoardSimpleResponseDto> findAllBoardLis(int page, BoardSearchDto dto) {
        return boardRepository.findAllBoards(PageRequest.of(page, 10, Sort.by("createAt").descending()), dto);
    }
    public Page<BoardSimpleResponseDto> findAllBoardListForUser(int page, BoardSearchDto dto, Long userId) {
        return boardRepository.findAllBoardsByUserId(PageRequest.of(page, 10, Sort.by("createAt").descending()), dto, userId);
    }

    public BoardResponseDto findBoard(int replyPage, Long boardId) {
        Board findBoard = verifyBoard(boardId);
        findBoard.setViewCount(findBoard.getViewCount()+1);
        Pageable replyPageable = PageRequest.of(replyPage, 10, Sort.by("createAt").descending());
        Page<BoardReplyResponseDto> reply = boardRepository.findAllBoardReply(replyPageable, boardId)
                .map(BoardReplyResponseDto::of);
        List<BoardFileResponseDto> file = boardRepository.findAllBoardFile(boardId)
                .stream().map(BoardFileResponseDto::of).toList();
        Board saveBoard = boardRepository.saveBoard(findBoard);
        return BoardResponseDto.of(saveBoard, reply, file);
    }

    public BoardResponseDto createBoard(BoardPostDto dto, User user) {
        Board newBoard = Board.create(dto, user.getUserId(), user.getUsername(), user.getNickName());
        return BoardResponseDto.of(boardRepository.saveBoard(newBoard), null, null);
    }

    public BoardResponseDto updateBoard(BoardPatchDto dto, Long boardId, User user) {
        Board findBoard = verifyBoard(boardId);
        if (!findBoard.getAuthorId().equals(user.getUserId()) || !user.getAuthority().equals(Authority.ADMIN)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
        Board update = findBoard.update(dto);
        Pageable replyPageable = PageRequest.of(0, 10, Sort.by("createAt").descending());
        Page<BoardReplyResponseDto> reply = boardRepository.findAllBoardReply(replyPageable, boardId)
                .map(BoardReplyResponseDto::of);
        List<BoardFileResponseDto> file = boardRepository.findAllBoardFile(boardId)
                .stream().map(BoardFileResponseDto::of).toList();
        return BoardResponseDto.of(boardRepository.saveBoard(update), reply, file);
    }

    public BoardResponseDto createOrUpdateBoardFile(Long boardId, MultipartFile[] file) {
        Board findBoard = verifyBoard(boardId);
        List<BoardFile> findBoardFile = boardRepository.findAllBoardFile(boardId);
        if (!findBoardFile.isEmpty()) {
            findBoardFile.forEach(m -> {
                        fileService.deleteAwsFile(m.getFileName(), AwsProperty.BOARD_FILE);
                        boardRepository.deleteBoardFile(m.getBoardFileId());
                    }
            );
        }
        List<BoardFileResponseDto> list = new ArrayList<>();
        for (MultipartFile multipartFile : file) {
            String url = fileService.saveMultipartFileForAws(multipartFile, AwsProperty.BOARD_FILE);
            String filename = multipartFile.getOriginalFilename();
            BoardFile newFile = BoardFile.create(boardId, filename, url);
            BoardFile saveFile = boardRepository.saveBoardFile(newFile);
            list.add(BoardFileResponseDto.of(saveFile));
        }
        Pageable replyPageable = PageRequest.of(0, 10, Sort.by("createAt").descending());
        Page<BoardReplyResponseDto> reply = boardRepository.findAllBoardReply(replyPageable, boardId)
                .map(BoardReplyResponseDto::of);

        return BoardResponseDto.of(boardRepository.saveBoard(findBoard), reply, list);
    }

    public BoardResponseDto createBoardReply(BoardReplyPostDto dto, Long boardId, User user) {
        Board findBoard = verifyBoard(boardId);
        BoardReply newReply = BoardReply.create(dto, boardId, user.getUserId(), user.getUsername(), user.getNickName());
        BoardReply saveReply = boardRepository.saveBoardReply(newReply);
        Pageable replyPageable = PageRequest.of(0, 10, Sort.by("createAt").descending());
        Page<BoardReplyResponseDto> reply = boardRepository.findAllBoardReply(replyPageable, boardId)
                .map(BoardReplyResponseDto::of);
        List<BoardFileResponseDto> file = boardRepository.findAllBoardFile(boardId)
                .stream().map(BoardFileResponseDto::of).toList();
        return BoardResponseDto.of(findBoard, reply, file);
    }

    public BoardResponseDto updateBoardReply(BoardReplyPatchDto dto, Long boardReplyId, User user) {
        BoardReply findBoardReply = verifyBoardReply(boardReplyId);
        Board findBoard = verifyBoard(findBoardReply.getBoardId());
        if (!findBoard.getAuthorId().equals(user.getUserId()) || !user.getAuthority().equals(Authority.ADMIN)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
        findBoardReply.setContent(dto.getContent());
        BoardReply saveReply = boardRepository.saveBoardReply(findBoardReply);
        Pageable replyPageable = PageRequest.of(0, 10, Sort.by("createAt").descending());
        Page<BoardReplyResponseDto> reply = boardRepository.findAllBoardReply(replyPageable, findBoard.getBoardId())
                .map(BoardReplyResponseDto::of);
        List<BoardFileResponseDto> file = boardRepository.findAllBoardFile(findBoard.getBoardId())
                .stream().map(BoardFileResponseDto::of).toList();
        return BoardResponseDto.of(findBoard, reply, file);
    }

    public void deleteBoardReply(Long boardReplyId) {
        boardRepository.deleteBoardReply(boardReplyId);
    }

    public void deleteBoard(Long boardId) {
        boardRepository.deleteBoard(boardId);
    }

    public void voteUser(Long boardReplyId, Long userId) {
        BoardReply findBoardReply = verifyBoardReply(boardReplyId);
        User user = userService.findUserEntity(userId);
        if (findBoardReply.getVoter().contains(user)) {
            findBoardReply.getVoter().remove(user);
        } else {
            findBoardReply.getVoter().add(user);
        }
        boardRepository.saveBoardReply(findBoardReply);
    }


    private Board verifyBoard(Long boardId) {
        return boardRepository.findBoard(boardId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));

    }

    private BoardReply verifyBoardReply(Long boardReplyId) {
        return boardRepository.findBoardReply(boardReplyId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));

    }

}
