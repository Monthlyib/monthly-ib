package com.monthlyib.server.api.news.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.board.dto.*;
import com.monthlyib.server.api.news.dto.*;
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

@Tag(name = "E. News", description = "입시뉴스 관련 API")
public interface NewsApiControllerIfs {

    class NewsListResponse extends PageResponseDto<List<NewsSimpleResponseDto>> { }

    @Operation(summary = "전체 입시뉴스 Data 요청(개인, 관리자)", description = "전체 입시뉴스 Data 리스트 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = NewsListResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<PageResponseDto<?>> getNewsList(
            @RequestParam(defaultValue = "0") int page,
            @ModelAttribute NewsSearchDto requestDto
    );

    class NewsResponse extends ResponseDto<NewsResponseDto> { }
    @Operation(summary = "입시뉴스 단건 조회(개인, 관리자)", description = "입시뉴스 단건 조회 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = NewsResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getNews(
            @PathVariable @Parameter(description = "News 식별자", required = true) Long newsId
    );


    @Operation(summary = "입시뉴스 데이터 생성(관리자)", description = "입시뉴스 데이터 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = NewsResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postNews(
            @RequestBody NewsPostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    // 뉴스 수정
    @Operation(summary = "입시뉴스 수정(관리자)", description = "입시뉴스 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = NewsResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchNews(
            @PathVariable @Parameter(description = "News 식별자", required = true) Long newsId,
            @RequestBody NewsPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    // 뉴스 파일 등록
    @Operation(summary = "입시뉴스 파일 등록/수정(관리자)", description = "입시뉴스 파일 등록 및 수정(이미 파일이 등록 되어있다면 업로드 파일로 수정됨)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = NewsResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postNewsFile(
            @PathVariable @Parameter(description = "News 식별자", required = true) Long newsId,
            @RequestPart("file") MultipartFile[] multipartFile,
            @UserSession @Parameter(hidden = true) User user
    );


    // 뉴스 삭제
    @Operation(summary = "입시뉴스 삭제(관리자)", description = "입시뉴스 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteNews(
            @PathVariable @Parameter(description = "News 식별자", required = true) Long newsId,
            @UserSession @Parameter(hidden = true) User user
    );



}
