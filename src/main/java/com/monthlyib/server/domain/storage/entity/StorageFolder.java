package com.monthlyib.server.domain.storage.entity;

import java.util.Optional;

import com.monthlyib.server.api.storage.dto.StoragePatchDto;
import com.monthlyib.server.api.storage.dto.StoragePostDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.StorageFolderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "storage_folder")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageFolder extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storageFolderId;

    @Column(nullable = false, length = 512)
    private String name;

    @Column(nullable = false, length = 1024)
    private String fullPath;

    @Column(nullable = false)
    private Long parentsFolderId;

    @Column(nullable = false, length = 255)
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
