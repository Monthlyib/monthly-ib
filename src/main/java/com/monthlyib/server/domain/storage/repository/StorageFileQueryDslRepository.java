package com.monthlyib.server.domain.storage.repository;

import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.storage.entity.QStorageFile;
import com.monthlyib.server.domain.storage.entity.StorageFile;
import com.monthlyib.server.exception.ServiceLogicException;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StorageFileQueryDslRepository extends QuerydslRepositorySupport {
    public StorageFileQueryDslRepository() {
        super(StorageFile.class);
    }

    QStorageFile file = QStorageFile.storageFile;

    public List<StorageFile> findAllStorageFile(Long parentsFolderId, String keyWord) {
        JPQLQuery<StorageFile> query = from(file).select(file);
        if (keyWord != null && !keyWord.isEmpty()) {
            if (parentsFolderId != null) {
                query.where(file.fileName.containsIgnoreCase(keyWord).and(file.parentsFolderId.eq(parentsFolderId)));
            } else {
                query.where(file.fileName.containsIgnoreCase(keyWord));
            }
        } else {
            if (parentsFolderId != null) {
                query.where(file.parentsFolderId.eq(parentsFolderId));
            }
        }

        return Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applySorting(Sort.by("fileName").ascending(), query)
                .fetch();
    }
}
