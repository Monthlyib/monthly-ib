package com.monthlyib.server.api.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardSimpleResponseDto {

    private Long boardId;

    private Long authorUserId;

    private String authorUsername;

    private String authorNickName;

    private String title;

    private String content;

    private long viewCount;

    private long replyCount;

    private long fileCount;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;


    public BoardSimpleResponseDto(
            Long boardId,
            Long authorUserId,
            String authorUsername,
            String authorNickName,
            String title,
            String content,
            long viewCount,
            LocalDateTime createAt,
            LocalDateTime updateAt
    ) {
        this.boardId = boardId;
        this.authorUserId = authorUserId;
        this.authorUsername = authorUsername;
        this.authorNickName = authorNickName;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

}
