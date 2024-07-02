package com.monthlyib.server.domain.storage.service;


import com.monthlyib.server.api.storage.dto.*;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.StorageFolderStatus;
import com.monthlyib.server.domain.storage.entity.StorageFile;
import com.monthlyib.server.domain.storage.entity.StorageFolder;
import com.monthlyib.server.domain.storage.repository.StorageRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final StorageRepository storageRepository;

    private final FileService fileService;

    public List<StorageFolderResponseDto> findAllMainFolder() {
        return storageRepository.findAllMainStorageFolder();
    }

    public StorageResponseDto findStorage(StorageSearchDto dto) {
        String keyWord = dto.getKeyWord();
        Long parentsFolderId = dto.getParentsFolderId();
        List<StorageFolderResponseDto> folder = storageRepository.findAllStorageFolder(parentsFolderId, keyWord)
                .stream().map(StorageFolderResponseDto::of).toList();
        List<StorageFileResponseDto> file = storageRepository.findAllStorageFile(parentsFolderId, keyWord)
                .stream().map(StorageFileResponseDto::of).toList();
        return StorageResponseDto.of(folder, file);
    }

    public StorageResponseDto createStorageFolder(StoragePostDto dto) {
        Long parentsFolderId = dto.getParentsFolderId();
        StorageFolderStatus status = dto.getStatus();
        String folderName = dto.getFolderName();
        StorageFolder newFolder = StorageFolder.create(dto);
        if (status.equals(StorageFolderStatus.SUB)) {
            StorageFolder folder = verifyStorageFolder(parentsFolderId);
            String path = getFullPath(parentsFolderId);
            newFolder.setFullPath(path+folderName+"/");
            newFolder.setParentsFolderId(folder.getStorageFolderId());
            newFolder.setParentsFolderName(folder.getName());
        } else {
            newFolder.setFullPath(folderName+"/");
            newFolder.setParentsFolderId(0L);
            newFolder.setParentsFolderName("MainFolder");
        }

        StorageFolder folder = storageRepository.saveStorageFolder(newFolder);
        return getResponse(folder);
    }

    public StorageResponseDto updateStorageFolder(StoragePatchDto dto) {
        Long storageFolderId = dto.getStorageFolderId();
        StorageFolder findFolder = verifyStorageFolder(storageFolderId);
        StorageFolder update = findFolder.update(dto);
        StorageFolder saveStorageFolder = storageRepository.saveStorageFolder(update);
        return getResponse(saveStorageFolder);
    }


    public void deleteFolder(Long storageFolderId) {
        StorageFolder findFolder = verifyStorageFolder(storageFolderId);
        storageRepository.deleteStorageFolder(findFolder.getStorageFolderId());
        fileService.deleteAwsDir(findFolder.getFullPath(), AwsProperty.STORAGE);
    }


    public StorageResponseDto uploadFile(Long parentsFolderId, MultipartFile[] files) {
        StorageFolder findFolder = verifyStorageFolder(parentsFolderId);
        String path = findFolder.getFullPath();
        String fullPath = AwsProperty.STORAGE.getName() + path;
        for (MultipartFile file : files) {
            String url = fileService.saveMultipartFileForAws(file, AwsProperty.STORAGE, path);
            String filename = file.getOriginalFilename();
            StorageFile newFile = StorageFile.create(filename, fullPath + filename, url, parentsFolderId, findFolder.getParentsFolderName());
            StorageFile storageFile = storageRepository.saveStorageFile(newFile);
        }
        return getResponse(findFolder);
    }

    public StorageResponseDto deleteFile(Long storageFileId) {
        StorageFile findFile = verifyStorageFile(storageFileId);
        Long parentsFolderId = findFile.getParentsFolderId();
        fileService.deleteAwsFile(findFile.getFullPath());
        storageRepository.deleteStorageFile(findFile.getStorageFileId());
        StorageFolder folder = verifyStorageFolder(parentsFolderId);
        return getResponse(folder);
    }


    private StorageResponseDto getResponse(StorageFolder folder) {
        StorageFolderStatus status = folder.getStatus();
        if (status.equals(StorageFolderStatus.MAIN)) {
            List<StorageFolderResponseDto> allMainFolder = findAllMainFolder();
            return StorageResponseDto.of(allMainFolder, null);
        } else {
            return findStorage(StorageSearchDto.builder().keyWord(null).parentsFolderId(folder.getParentsFolderId()).build());
        }
    }

    private StorageFolder verifyStorageFolder(Long storageFolderId) {
        return storageRepository.findStorageFolder(storageFolderId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_STORAGE_FOLDER));
    }

    private StorageFile verifyStorageFile(Long storageFileId) {
        return storageRepository.findStorageFile(storageFileId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_STORAGE_FILE));
    }

    private String getFullPath(Long parentsId) {
        Optional<StorageFolder> storageFolder = storageRepository.findStorageFolder(parentsId);
        StringBuilder sb = new StringBuilder();
        if (storageFolder.isPresent()) {
            StorageFolder folder = storageFolder.get();
            if (folder.getStatus().equals(StorageFolderStatus.MAIN)) {
                return sb.append(folder.getName()).append("/").toString();
            } else {
                String path = getFullPath(folder.getParentsFolderId());
                sb.append(path).append(folder.getName()).append("/");
            }
        }
        return sb.toString();
    }
}
