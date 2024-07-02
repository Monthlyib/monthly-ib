package com.monthlyib.server.domain.storage.repository;

import com.monthlyib.server.domain.storage.entity.StorageFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageFileJpaRepository extends JpaRepository<StorageFile, Long> {
}
