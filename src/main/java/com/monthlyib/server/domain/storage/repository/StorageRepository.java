package com.monthlyib.server.domain.storage.repository;

import com.monthlyib.server.api.storage.dto.StorageFolderResponseDto;
import com.monthlyib.server.api.storage.dto.StorageSearchDto;
import com.monthlyib.server.domain.storage.entity.StorageFile;
import com.monthlyib.server.domain.storage.entity.StorageFolder;

import java.util.List;
import java.util.Optional;

public interface StorageRepository {

    List<StorageFolderResponseDto> findAllMainStorageFolder();

    List<StorageFolder> findAllStorageFolder(Long parentsFolderId, String keyWord);

    List<StorageFile> findAllStorageFile(Long parentsFolderId, String keyWord);

    Optional<StorageFolder> findStorageFolder(Long storageFolderId);

    Optional<StorageFile> findStorageFile(Long storageFileId);

    StorageFile saveStorageFile(StorageFile storageFile);

    StorageFolder saveStorageFolder(StorageFolder storageFolder);

    void deleteStorageFolder(Long storageFolderId);

    void deleteStorageFile(Long storageFileId);
}
