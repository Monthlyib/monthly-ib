package com.monthlyib.server.api.board.dto;

import com.monthlyib.server.domain.board.entity.BoardReply;
import com.monthlyib.server.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardReplyResponseDto {

    private Long boardReplyId;

    private Long boardId;

    private Long authorId;

    private String authorUsername;

    private String authorNickname;

    private String content;

    private List<Long> voteUserId;

    private long voterCount;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public static BoardReplyResponseDto of(BoardReply boardReply) {
        return BoardReplyResponseDto.builder()
                .boardReplyId(boardReply.getBoardReplyId())
                .boardId(boardReply.getBoardId())
                .authorId(boardReply.getAuthorId())
                .authorUsername(boardReply.getAuthorUsername())
                .authorNickname(boardReply.getAuthorNickName())
                .content(boardReply.getContent())
                .voteUserId(new ArrayList<>(boardReply.getVoter().stream().map(User::getUserId).toList()))
                .voterCount(boardReply.getVoter().size())
                .createAt(boardReply.getCreateAt())
                .updateAt(boardReply.getUpdateAt())
                .build();
    }

}
