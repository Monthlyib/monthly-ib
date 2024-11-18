package com.monthlyib.server.domain.storage.entity;


import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fullPath;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private Long parentsFolderId;

    @Column(nullable = false)
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
