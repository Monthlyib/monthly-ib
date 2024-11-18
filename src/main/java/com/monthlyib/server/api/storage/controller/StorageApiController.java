package com.monthlyib.server.api.storage.controller;


import com.monthlyib.server.api.storage.dto.*;
import com.monthlyib.server.domain.storage.service.StorageService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class StorageApiController implements StorageApiControllerIfs{

    private final StorageService storageService;

    @Override
    @GetMapping("/open-api/storage")
    public ResponseEntity<ResponseDto<?>> getStorageMainFolder() {
        List<StorageFolderResponseDto> response = storageService.findAllMainFolder();
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @GetMapping("/open-api/storage/detail")
    public ResponseEntity<ResponseDto<?>> getStorage(StorageSearchDto requestDto) {
        StorageResponseDto response = storageService.findStorage(requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/storage")
    public ResponseEntity<ResponseDto<?>> postStorageFolder(StoragePostDto requestDto, User user) {
        StorageResponseDto response = storageService.createStorageFolder(requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/storage")
    public ResponseEntity<ResponseDto<?>> patchStorageFolder(StoragePatchDto requestDto, User user) {
        StorageResponseDto response = storageService.updateStorageFolder(requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/storage/{storageFolderId}")
    public ResponseEntity<ResponseDto<?>> deleteStorageFolder(Long storageFolderId, User user) {
        storageService.deleteFolder(storageFolderId);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @PostMapping("/api/storage/file/{parentsFolderId}")
    public ResponseEntity<ResponseDto<?>> postStorageFile(Long parentsFolderId, MultipartFile[] multipartFile, User user) {
        StorageResponseDto response = storageService.uploadFile(parentsFolderId, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/storage/file/{storageFileId}")
    public ResponseEntity<ResponseDto<?>> deleteStorageFile(Long storageFileId, User user) {
        StorageResponseDto response = storageService.deleteFile(storageFileId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }
}
