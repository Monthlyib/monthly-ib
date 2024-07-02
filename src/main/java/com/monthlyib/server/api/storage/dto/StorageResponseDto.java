package com.monthlyib.server.api.storage.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorageResponseDto {

    private List<StorageFolderResponseDto> folders;

    private List<StorageFileResponseDto> files;

    public static StorageResponseDto of(List<StorageFolderResponseDto> folders, List<StorageFileResponseDto> files) {
        return StorageResponseDto.builder()
                .folders(folders)
                .files(files)
                .build();
    }

}
