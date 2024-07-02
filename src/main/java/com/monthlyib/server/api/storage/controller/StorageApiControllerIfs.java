package com.monthlyib.server.api.storage.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.storage.dto.*;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ErrorResponse;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "I. Storage", description = "자료실 API")
public interface StorageApiControllerIfs {

    class MainFolderResponse extends ResponseDto<List<StorageFolderResponseDto>> { }


    @Operation(summary = "자료실 메인 페이지 조회 요청(개인, 관리자)", description = "자료실 메인 페이지(Main Folder) Data 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = MainFolderResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getStorageMainFolder(
    );

    class StorageResponse extends ResponseDto<StorageResponseDto> { }

    @Operation(summary = "자료실 전체 조회/검색 요청(개인, 관리자)", description = "자료실 전체 조회/검색 Data 요청, parentsFolderId & keyWord 값 모두 있을때 해당 폴더 내부에서 키워드 검색, keyWord 값만 있을시 자료실 전체에서 해당 키워드로 검색, parentsFolderId 값만 있을 경우 해당 폴더 하위 데이터 모두 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = StorageResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getStorage(
            @ModelAttribute StorageSearchDto requestDto
    );

    @Operation(summary = "자료실 폴더 데이터 생성(관리자)", description = "자료실 폴더 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = StorageResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postStorageFolder(
            @RequestBody StoragePostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "자료실 폴더 데이터 수정(관리자)", description = "자료실 폴더 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = StorageResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchStorageFolder(
            @RequestBody StoragePatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "자료실 폴더 삭제(관리자)", description = "자료실 폴더 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteStorageFolder(
            @PathVariable @Parameter(description = "자료실 Folder 식별자", required = true) Long storageFolderId,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "자료실 파일 등록(관리자)", description = "자료실 파일 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = StorageResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postStorageFile(
            @PathVariable @Parameter(description = "Folder 식별자", required = true) Long parentsFolderId,
            @RequestPart("file") MultipartFile[] multipartFile,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "자료실 파일 삭제(관리자)", description = "자료실 파일 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = StorageResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteStorageFile(
            @PathVariable @Parameter(description = "자료실 File 식별자", required = true) Long storageFileId,
            @UserSession @Parameter(hidden = true) User user
    );
}
