package com.monthlyib.server.api.storage.dto;


import com.monthlyib.server.domain.storage.entity.StorageFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorageFileResponseDto {

    private Long fileId;

    private String fileName;

    private String fileUrl;

    private Long parentsFolderId;

    private String parentsFolderName;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public static StorageFileResponseDto of(StorageFile storageFile) {
        return StorageFileResponseDto.builder()
                .fileId(storageFile.getStorageFileId())
                .fileName(storageFile.getFileName())
                .fileUrl(storageFile.getFileUrl())
                .parentsFolderId(storageFile.getParentsFolderId())
                .parentsFolderName(storageFile.getParentsFolderName())
                .createAt(storageFile.getCreateAt())
                .updateAt(storageFile.getUpdateAt())
                .build();
    }


}
