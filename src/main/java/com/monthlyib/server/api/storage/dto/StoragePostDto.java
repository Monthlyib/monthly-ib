package com.monthlyib.server.api.storage.dto;


import com.monthlyib.server.constant.StorageFolderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoragePostDto {

    private Long parentsFolderId;

    private String folderName;

    private StorageFolderStatus status;

}
