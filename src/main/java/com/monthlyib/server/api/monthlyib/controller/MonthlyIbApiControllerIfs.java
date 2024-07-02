package com.monthlyib.server.api.monthlyib.controller;


import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.monthlyib.dto.*;
import com.monthlyib.server.api.user.controller.UserApiControllerIfs;
import com.monthlyib.server.api.user.dto.UserPatchRequestDto;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "C. MonthlyIB", description = "월간 IB 데이터 관련")
public interface MonthlyIbApiControllerIfs {


    class Response extends ResponseDto<MonthlyIbResponseDto> {}

    class PageResponse extends PageResponseDto<List<MonthlyIbSimpleResponseDto>> {}

    @Operation(summary = "전체 MonthlyIB Data 요청(개인, 관리자)", description = "전체 MonthlyIB Data 리스트 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = PageResponse.class)

                    )
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<PageResponseDto<?>> getMonthlyIb(
            @RequestParam(defaultValue = "0") int page,
            @ModelAttribute MonthlyIbSearchDto requestDto
            );

    // 단건 조회
    @Operation(summary = "MonthlyIB 단건 조회(개인, 관리자)", description = "MonthlyIB 단건 조회 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = Response.class)
                    )
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getMonthlyIb(
            @PathVariable @Parameter(description = "MonthlyIB 식별자", required = true) Long monthlyIbId
    );

    // 생성
    @Operation(summary = "MonthlyIB 생성(관리자)", description = "MonthlyIB 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = Response.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postMonthlyIb(
            @RequestBody MonthlyIbPostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    // 썸네일 이미지 등록
    @Operation(summary = "MonthlyIB 썸네일 이미지 등록/수정(관리자)", description = "MonthlyIB 썸네일 이미지 등록 및 수정(이미 이미지가 등록 되어있다면 업로드 이미지로 수정됨)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = Response.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postMonthlyIbImage(
            @PathVariable @Parameter(description = "MonthlyIB 식별자", required = true) Long monthlyIbId,
            @RequestPart("image") MultipartFile[] multipartFile,
            @UserSession @Parameter(hidden = true) User user
    );

    //PDF 파일 등록
    @Operation(summary = "MonthlyIB PDF 파일 등록/수정(관리자)", description = "MonthlyIB PDF 파일 등록 및 수정(이미 파일이 등록 되어있다면 업로드 파일로 수정됨)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = Response.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postMonthlyIbPdf(
            @PathVariable @Parameter(description = "MonthlyIB 식별자", required = true) Long monthlyIbId,
            @RequestPart("file") MultipartFile[] multipartFile,
            @UserSession @Parameter(hidden = true) User user
    );


    // 수정
    @Operation(summary = "MonthlyIB 수정(관리자)", description = "MonthlyIB 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = Response.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchMonthlyIb(
            @PathVariable @Parameter(description = "MonthlyIB 식별자", required = true) Long monthlyIbId,
            @RequestBody MonthlyIbPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    // 삭제
    @Operation(summary = "MonthlyIB 삭제(관리자)", description = "MonthlyIB 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteMonthlyIb(
            @PathVariable @Parameter(description = "MonthlyIB 식별자", required = true) Long monthlyIbId,
            @UserSession @Parameter(hidden = true) User user
    );
}
