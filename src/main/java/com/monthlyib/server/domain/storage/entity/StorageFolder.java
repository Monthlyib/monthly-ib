package com.monthlyib.server.domain.storage.entity;

import com.monthlyib.server.api.storage.dto.StoragePatchDto;
import com.monthlyib.server.api.storage.dto.StoragePostDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.StorageFolderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "storage_folder")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageFolder extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storageFolderId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String fullPath;

    @Column(nullable = false)
    private Long parentsFolderId;

    @Column(nullable = false)
    private String parentsFolderName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StorageFolderStatus status;

    public static StorageFolder create(StoragePostDto dto) {
        return StorageFolder.builder()
                .name(dto.getFolderName())
                .status(dto.getStatus())
                .build();
    }

    public StorageFolder update(StoragePatchDto dto) {
        this.name = Optional.ofNullable(dto.getFolderName()).orElse(this.name);
        this.status = Optional.ofNullable(dto.getStatus()).orElse(this.status);
        return this;
    }

}
