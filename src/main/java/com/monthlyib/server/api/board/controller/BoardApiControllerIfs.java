package com.monthlyib.server.api.board.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.board.dto.*;
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

@Tag(name = "D. Board", description = "자유게시판 관련 API")
public interface BoardApiControllerIfs {

    class BoardListResponse extends PageResponseDto<List<BoardSimpleResponseDto>> { }

    @Operation(summary = "전체 자유게시판 Data 요청(개인, 관리자)", description = "전체 자유게시판 Data 리스트 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = BoardListResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<PageResponseDto<?>> getBoardList(
            @RequestParam(defaultValue = "0") int page,
            @ModelAttribute BoardSearchDto requestDto
    );

    @Operation(summary = "특정 회원 자유게시판 Data 요청(개인, 관리자)", description = "전체 자유게시판 Data 리스트 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = BoardListResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<PageResponseDto<?>> getBoardListForUser(
            @RequestParam(defaultValue = "0") int page,
            @ModelAttribute BoardSearchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    class BoardResponse extends ResponseDto<BoardResponseDto> { }
    @Operation(summary = "자유게시판 단건 조회(개인, 관리자)", description = "자유게시판 단건 조회 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = BoardResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getBoard(
            @RequestParam(defaultValue = "0") int replyPage,
            @PathVariable @Parameter(description = "Board 식별자", required = true) Long boardId
    );


    @Operation(summary = "자유게시판 데이터 생성(개인, 관리자)", description = "자유게시판 데이터 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = BoardResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postBoard(
            @RequestBody BoardPostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    // 게시글 수정
    @Operation(summary = "게시글 수정(개인, 관리자)", description = "게시글 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = BoardResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchBoard(
            @PathVariable @Parameter(description = "Board 식별자", required = true) Long boardId,
            @RequestBody BoardPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    // 게시글 파일 등록
    @Operation(summary = "게시글 파일 등록/수정(개인, 관리자)", description = "게시글 파일 등록 및 수정(이미 파일이 등록 되어있다면 업로드 파일로 수정됨)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postBoardFile(
            @PathVariable @Parameter(description = "Board 식별자", required = true) Long boardId,
            @RequestPart("file") MultipartFile[] multipartFile,
            @UserSession @Parameter(hidden = true) User user
    );

    // 답글 생성
    @Operation(summary = "자유게시판 답글 데이터 생성(개인, 관리자)", description = "자유게시판 답글 데이터 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postBoardReply(
            @PathVariable @Parameter(description = "Board 식별자", required = true) Long boardId,
            @RequestBody BoardReplyPostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    // 답글 수정

    @Operation(summary = "게시글 답글 수정(개인, 관리자)", description = "게시글 답글 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchBoardReply(
            @PathVariable @Parameter(description = "BoardReply 식별자", required = true) Long boardReplyId,
            @RequestBody BoardReplyPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    // 게시글 삭제
    @Operation(summary = "게시글 삭제(개인, 관리자)", description = "게시글 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteBoard(
            @PathVariable @Parameter(description = "Board 식별자", required = true) Long boardId,
            @UserSession @Parameter(hidden = true) User user
    );

    // 답글 삭제
    @Operation(summary = "게시글 답글 삭제(개인, 관리자)", description = "게시글 답글 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteBoardReply(
            @PathVariable @Parameter(description = "BoardReply 식별자", required = true) Long boardReplyId,
            @UserSession @Parameter(hidden = true) User user
    );


    // 답글 추천/추천 취소
    @Operation(summary = "게시글 답글 추천/추천 취소(개인, 관리자)", description = "게시글 답글 추천(좋아요) 요청 이미 추천한 회원은 추천 취소됨")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> voteBoardReply(
            @PathVariable @Parameter(description = "BoardReply 식별자", required = true) Long boardReplyId,
            @UserSession @Parameter(hidden = true) User user
    );


}
