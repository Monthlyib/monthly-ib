package com.monthlyib.server.api.videolessons.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.videolessons.dto.*;
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

@Tag(name = "G. Video Lessons", description = "영상 강의 관련 API")
public interface VideoApiControllerIfs {


    class VideoLessonsListResponse extends PageResponseDto<List<VideoLessonsSimpleResponseDto>> { }
    class VideoLessonsUserListResponse extends PageResponseDto<List<VideoLessonsUserSimpleResponseDto>> { }

    @Operation(summary = "전체 영상강의 Data 요청(개인, 관리자)", description = "전체 영상강의 Data 리스트 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoLessonsListResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<PageResponseDto<?>> getVideoLessonsList(
            @ModelAttribute VideoLessonsSearchDto requestDto
    );

    // 영상강의 상세 조회

    class VideoLessonsResponse extends ResponseDto<VideoLessonsResponseDto> { }
    @Operation(summary = "영상강의 단건 조회(개인, 관리자)", description = "영상강의 단건 조회 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoLessonsResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getVideoLessons(
            @RequestParam(defaultValue = "0") int replyPage,
            @PathVariable @Parameter(description = "VideoLessons 식별자", required = true) Long videoLessonsId
    );

    // 영상 강의 생성
    @Operation(summary = "영상강의 데이터 생성(관리자)", description = "영상강의 데이터 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoLessonsResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postVideoLessons(
            @RequestBody VideoLessonsPostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "수강중 영상강의 조회(개인, 관리자)", description = "특정 회원의 수강중 영상강의 조회 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoLessonsUserListResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<PageResponseDto<?>> getVideoLessonsForUser(
            @PathVariable @Parameter(description = "User 식별자", required = true) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "영상강의 수강 신청(개인)", description = "영상강의 수강 신청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoLessonsResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postVideoLessonsForUser(
            @PathVariable @Parameter(description = "VideoLessons 식별자", required = true) Long videoLessonsId,
            @UserSession @Parameter(hidden = true) User user
    );

    // 영상 강의 썸네일 등록
    @Operation(summary = "영상강의 썸네일 이미지 등록/수정(관리자)", description = "영상강의 썸네일 이미지 등록 및 수정(이미 이미지가 등록 되어있다면 업로드 이미지로 수정됨)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postVideoLessonsImage(
            @PathVariable @Parameter(description = "VideoLessons 식별자", required = true) Long videoLessonsId,
            @RequestPart("image") MultipartFile[] multipartFile,
            @UserSession @Parameter(hidden = true) User user
    );
    // 챕터별 영상 강의 파일 등록/수정
    @Operation(summary = "영상강의 파일 등록/수정(관리자)", description = "영상강의 파일 등록 및 수정(이미 파일이 등록 되어있다면 업로드 파일로 수정됨)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postVideoFile(
            @PathVariable @Parameter(description = "SubChapter 식별자", required = true) Long chapterId,
            @RequestPart("file") MultipartFile[] multipartFile,
            @UserSession @Parameter(hidden = true) User user
    );

    // 영상 강의 수정
    @Operation(summary = "영상강의 수정(관리자)", description = "영상강의 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoLessonsResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchVideo(
            @RequestBody VideoLessonsPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    // 영상 강의 삭제
    @Operation(summary = "영상강의 삭제(개인, 관리자)", description = "영상강의 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteVideoLessons(
            @PathVariable @Parameter(description = "VideoLessons 식별자", required = true) Long videoLessonsId,
            @UserSession @Parameter(hidden = true) User user
    );

    class VideoLessonsReplyResponse extends ResponseDto<VideoLessonsReplyResponseDto> { }
    // 수강 후기 등록
    @Operation(summary = "영상강의 리뷰 데이터 생성(개인, 관리자)", description = "영상강의 리뷰 데이터 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoLessonsReplyResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postVideoLessonsReply(
            @RequestBody VideoLessonsReplyPostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );
    // 수강 후기 수정
    @Operation(summary = "영상강의 리뷰 데이터 수정(개인, 관리자)", description = "영상강의 리뷰 데이터 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoLessonsReplyResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchVideoLessonsReply(
            @RequestBody VideoLessonsReplyPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );
    // 수강 후기 삭제
    @Operation(summary = "영상강의 리뷰 삭제(개인, 관리자)", description = "영상강의 리뷰 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteVideoReply(
            @PathVariable @Parameter(description = "VideoLessonsReply 식별자", required = true) Long videoLessonsReplyId,
            @UserSession @Parameter(hidden = true) User user
    );
    // 수강 후기 추천/추천 취소
    @Operation(summary = "영상강의 리뷰 추천/추천 취소(개인, 관리자)", description = "영상강의 리뷰 추천(좋아요) 요청 이미 추천한 회원은 추천 취소됨")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> voteVideoReply(
            @PathVariable @Parameter(description = "VideoLessonsReply 식별자", required = true) Long videoLessonsReplyId,
            @UserSession @Parameter(hidden = true) User user
    );
    class VideoCategoryResponse extends ResponseDto<VideoCategoryDetailResponseDto> { }
    // 영상 강의 카테고리 조회
    @Operation(summary = "영상강의 카테고리 조회(개인, 관리자)", description = "영상강의 카테고리 상태별 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoCategoryResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getVideoCategory(
    );

    class VideoLessonsCategoryResponse extends ResponseDto<VideoCategoryResponseDto> { }
    // 영상 강의 카테고리 생성
    @Operation(summary = "영상강의 카테고리 데이터 생성(관리자)", description = "영상강의 카테고리 데이터 생성 요청, 최 상위 카테고리는 parentsId = 0 그 외 parentsId 값 기입")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoLessonsCategoryResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postVideoLessonsCategory(
            @RequestBody VideoCategoryPostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );
    // 영상 강의 카테고리 수정
    @Operation(summary = "영상강의 카테고리 데이터 수정(관리자)", description = "영상강의 카테고리 데이터 수정 요청, 최 상위 카테고리는 parentsId = 0 그 외 parentsId 값 기입")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = VideoLessonsCategoryResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchVideoLessonsCategory(
            @RequestBody VideoCategoryPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    // 영상 강의 카테고리 삭제
    @Operation(summary = "영상강의 카테고리 삭제(개인, 관리자)", description = "영상강의 카테고리 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteVideoCategory(
            @PathVariable @Parameter(description = "VideoLessonsCategory 식별자", required = true) Long videoCategoryId,
            @UserSession @Parameter(hidden = true) User user
    );
}
