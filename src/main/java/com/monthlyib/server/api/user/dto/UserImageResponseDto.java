package com.monthlyib.server.api.user.dto;

import com.monthlyib.server.domain.board.entity.BoardFile;
import com.monthlyib.server.domain.user.entity.UserImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImageResponseDto {

    private Long fileId;

    private String fileName;

    private String fileUrl;

    public static UserImageResponseDto of(UserImage image) {
        return UserImageResponseDto.builder()
                .fileId(image.getUserImageId())
                .fileName(image.getFileName())
                .fileUrl(image.getFileUrl())
                .build();
    }

}
