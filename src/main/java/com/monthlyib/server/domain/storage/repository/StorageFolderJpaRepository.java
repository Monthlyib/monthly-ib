package com.monthlyib.server.domain.storage.repository;

import com.monthlyib.server.domain.storage.entity.StorageFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageFolderJpaRepository extends JpaRepository<StorageFolder, Long> {
}
