package com.monthlyib.server.api.board.dto;

import com.monthlyib.server.domain.board.entity.BoardFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardFileResponseDto {

    private Long fileId;

    private String fileName;

    private String fileUrl;

    public static BoardFileResponseDto of(BoardFile boardFile) {
        return BoardFileResponseDto.builder()
                .fileId(boardFile.getBoardFileId())
                .fileName(boardFile.getFileName())
                .fileUrl(boardFile.getUrl())
                .build();
    }

}
