package com.monthlyib.server.domain.storage.entity;


import com.monthlyib.server.audit.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "storage_file")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageFile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storageFileId;

    @Column(nullable = false, length = 512)
    private String fileName;

    @Column(nullable = false, length = 1024)
    private String fullPath;

    @Column(nullable = false, length = 2048)
    private String fileUrl;

    @Column(nullable = false)
    private Long parentsFolderId;

    @Column(nullable = false, length = 255)
    private String parentsFolderName;

    public static StorageFile create(String fileName, String fullPath, String fileUrl, Long parentsFolderId, String parentsFolderName) {
        return StorageFile.builder()
                .fileName(fileName)
                .fullPath(fullPath)
                .fileUrl(fileUrl)
                .parentsFolderId(parentsFolderId)
                .parentsFolderName(parentsFolderName)
                .build();
    }
}
