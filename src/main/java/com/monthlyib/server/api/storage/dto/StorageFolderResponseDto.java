package com.monthlyib.server.api.storage.dto;


import com.monthlyib.server.constant.StorageFolderStatus;
import com.monthlyib.server.domain.storage.entity.StorageFolder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorageFolderResponseDto {

    private Long folderId;

    private String name;

    private Long parentsFolderId;

    private String parentsFolderName;

    private StorageFolderStatus status;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public static StorageFolderResponseDto of(StorageFolder folder) {
        return StorageFolderResponseDto.builder()
                .folderId(folder.getStorageFolderId())
                .name(folder.getName())
                .parentsFolderId(folder.getParentsFolderId())
                .parentsFolderName(folder.getParentsFolderName())
                .status(folder.getStatus())
                .createAt(folder.getCreateAt())
                .updateAt(folder.getUpdateAt())
                .build();
    }


}
