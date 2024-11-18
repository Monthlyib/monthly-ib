package com.monthlyib.server.api.board.dto;

import com.monthlyib.server.domain.board.entity.Board;
import com.monthlyib.server.domain.board.entity.BoardFile;
import com.monthlyib.server.domain.board.entity.BoardReply;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardResponseDto {

    private Long boardId;

    private Long authorUserId;

    private String authorUsername;

    private String authorNickName;

    private String title;

    private String content;

    private long viewCount;

    private PageResponseDto<List<BoardReplyResponseDto>> reply;

    private List<BoardFileResponseDto> files;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public static BoardResponseDto of(Board board, Page<BoardReplyResponseDto> reply, List<BoardFileResponseDto> file) {
        BoardResponseDto response = BoardResponseDto.builder()
                .boardId(board.getBoardId())
                .authorUserId(board.getAuthorId())
                .authorUsername(board.getAuthorUsername())
                .authorNickName(board.getAuthorNickName())
                .title(board.getTitle())
                .content(board.getContent())
                .viewCount(board.getViewCount())
                .files(file)
                .createAt(board.getCreateAt())
                .updateAt(board.getUpdateAt())
                .build();
        if (reply != null) {
            response.setReply(PageResponseDto.of(reply, reply.getContent(), Result.ok()));
        }
        return response;
    }

}
