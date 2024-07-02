package com.monthlyib.server.domain.storage.repository;

import com.monthlyib.server.api.storage.dto.StorageFolderResponseDto;
import com.monthlyib.server.api.tutoring.dto.TutoringResponseDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.StorageFolderStatus;
import com.monthlyib.server.domain.storage.entity.QStorageFolder;
import com.monthlyib.server.domain.storage.entity.StorageFile;
import com.monthlyib.server.domain.storage.entity.StorageFolder;
import com.monthlyib.server.exception.ServiceLogicException;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class StorageRepositoryImpl extends QuerydslRepositorySupport implements StorageRepository {

    private final StorageFolderJpaRepository storageFolderJpaRepository;

    private final StorageFileJpaRepository storageFileJpaRepository;

    private final StorageFileQueryDslRepository storageFileQueryDslRepository;

    public StorageRepositoryImpl(
            StorageFolderJpaRepository storageFolderJpaRepository,
            StorageFileJpaRepository storageFileJpaRepository,
            StorageFileQueryDslRepository storageFileQueryDslRepository
    ) {
        super(StorageFolder.class);
        this.storageFolderJpaRepository = storageFolderJpaRepository;
        this.storageFileJpaRepository = storageFileJpaRepository;
        this.storageFileQueryDslRepository = storageFileQueryDslRepository;
    }

    QStorageFolder folder = QStorageFolder.storageFolder;
    @Override
    public List<StorageFolderResponseDto> findAllMainStorageFolder() {
        JPQLQuery<StorageFolderResponseDto> query = getStorageFolderQuery();
        query.where(folder.status.eq(StorageFolderStatus.MAIN));

        return Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applySorting(Sort.by("name").ascending(), query)
                .fetch();
    }

    @Override
    public List<StorageFolder> findAllStorageFolder(Long parentsFolderId, String keyWord) {
        JPQLQuery<StorageFolder> query = from(folder).select(folder);
        if (keyWord != null && !keyWord.isEmpty()) {
            if (parentsFolderId != null) {
                query.where(folder.name.containsIgnoreCase(keyWord).and(folder.parentsFolderId.eq(parentsFolderId)));
            } else {
                query.where(folder.name.containsIgnoreCase(keyWord));
            }
        } else {
            if (parentsFolderId != null) {
                query.where(folder.parentsFolderId.eq(parentsFolderId));
            }
        }

        return Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applySorting(Sort.by("name").ascending(), query)
                .fetch();
    }

    @Override
    public List<StorageFile> findAllStorageFile(Long parentsFolderId, String keyWord) {
        return storageFileQueryDslRepository.findAllStorageFile(parentsFolderId, keyWord);
    }

    @Override
    public Optional<StorageFolder> findStorageFolder(Long storageFolderId) {
        return storageFolderJpaRepository.findById(storageFolderId);
    }

    @Override
    public Optional<StorageFile> findStorageFile(Long storageFileId) {
        return storageFileJpaRepository.findById(storageFileId);
    }

    @Override
    public StorageFile saveStorageFile(StorageFile storageFile) {
        return storageFileJpaRepository.save(storageFile);
    }

    @Override
    public StorageFolder saveStorageFolder(StorageFolder storageFolder) {
        return storageFolderJpaRepository.save(storageFolder);
    }

    @Override
    public void deleteStorageFolder(Long storageFolderId) {
        Set<StorageFolder> allFolders = getAllFoldersForDelete(storageFolderId);
        storageFolderJpaRepository.deleteAll(allFolders);
    }


    private Set<StorageFolder> getAllFoldersForDelete(Long storageFolderId) {
        Set<StorageFolder> folders = new LinkedHashSet<>();
        folders.add(findStorageFolder(storageFolderId).orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_STORAGE_FOLDER)));
        Set<StorageFolder> findAll = new LinkedHashSet<>(findAllStorageFolder(storageFolderId, null));
        if (!findAll.isEmpty()) {
            folders.addAll(findAll);
            findAll.forEach(f -> {
                Set<StorageFolder> allFolders = getAllFoldersForDelete(f.getStorageFolderId());
                folders.addAll(allFolders);
                List<StorageFile> allStorageFile = findAllStorageFile(f.getStorageFolderId(), null);
                storageFileJpaRepository.deleteAll(allStorageFile);
            });
        }
        return folders;
    }

    @Override
    public void deleteStorageFile(Long storageFileId) {
        storageFileJpaRepository.deleteById(storageFileId);
    }

    private JPQLQuery<StorageFolderResponseDto> getStorageFolderQuery() {
        return from(folder)
                .select(
                        Projections.constructor(
                                StorageFolderResponseDto.class,
                                folder.storageFolderId,
                                folder.name,
                                folder.parentsFolderId,
                                folder.parentsFolderName,
                                folder.status,
                                folder.createAt,
                                folder.updateAt
                        )
                );
    }
}
