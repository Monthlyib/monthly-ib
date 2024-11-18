package com.monthlyib.server.api.tutoring.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.tutoring.dto.*;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ErrorResponse;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
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

import java.util.List;

@Tag(name = "F. Tutoring", description = "튜터링 관련 API")
public interface TutoringControllerIfs {

    // 날짜별 튜터링 간단 조회(튜터링 식별자 리스트, 남은 예약수, 총 예약수)
    class TutoringSimple extends ResponseDto<TutoringSimpleResponseDto> { }
    @Operation(summary = "날짜별 튜터링 Simple Data 요청(개인, 관리자)", description = "날짜별 튜터링 Data 리스트 요청, 예약 가능 여부 체크 용도 / 특정 날짜, 시간에 데이터 없다면 예약 없음, date 필드 필수값")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = TutoringSimple.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getDateTutoring(
            @ModelAttribute TutoringSearchDto requestDto
    );
    // 날짜별 튜터링 리스트 전체 조회
    class Tutoring extends ResponseDto<List<TutoringDetailResponseDto>> { }
    @Operation(summary = "날짜별 튜터링 Data 요청(개인, 관리자)", description = "날짜별 튜터링 Data 리스트 요청, date, tutoringStatus 필드 값 없을시(null) 해당 회원의 전체 튜터링 데이터 응답, 관리자일 경우 전체 데이터 응답,1페이지당 30개 데이터 응답")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = Tutoring.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getDateTutoringList(
            @ModelAttribute TutoringAdminSearchDto requestDto,
            @UserSession @Parameter(hidden = true) User user

    );

    // 시간별 튜텨링 리스트 조회
    class TutoringTime extends ResponseDto<List<TutoringResponseDto>> { }
    @Operation(summary = "특정 시간 튜터링 Data 요청(관리자)", description = "특정 시간 튜터링 Data 리스트 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = TutoringTime.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getTimeTutoringList(
            @ModelAttribute TutoringTimeSearchDto requestDto
    );
    // 특정 회원의 튜터링 전체 조회
    class TutoringUser extends PageResponseDto<List<TutoringResponseDto>> { }
    @Operation(summary = "특정 회원 튜터링 Data 요청(개인, 관리자)", description = "특정 회원 튜터링 Data 리스트 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = TutoringUser.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @Hidden
    ResponseEntity<PageResponseDto<?>> getUserTutoringList(
            @PathVariable @Parameter(description = "User 식별자", required = true) Long userId,
            @ModelAttribute TutoringUserSearchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );
    // 튜터링 생성 요청
    class TutoringDetail extends ResponseDto<TutoringResponseDto> { }
    @Operation(summary = "튜터링 생성 요청(개인, 관리자)", description = "튜터링 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = TutoringDetail.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postTutoring(
            @RequestBody TutoringPostRequestDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );
    // 튜터링 수정 요청
    @Operation(summary = "튜터링 수정 요청(개인, 관리자)", description =
            """
                    튜터링 수정 요청\n
                    - 개인 : 본인의 튜터링 detail 만 수정가능(상태 필드 null 값으로 요청)
                    - 관리자 : 튜터링 내용 및 상태 변경 가능
                    - 시간 변경 시 삭제 후 재 요청 필요
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = TutoringDetail.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchTutoring(
            @PathVariable @Parameter(description = "Tutoring 식별자", required = true) Long tutoringId,
            @RequestBody TutoringPatchRequestDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );
    // 튜터링 취소 요청
    @Operation(summary = "튜터링 삭제(개인, 관리자)", description = "튜터링 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteNews(
            @PathVariable @Parameter(description = "Tutoring 식별자", required = true) Long tutoringId,
            @UserSession @Parameter(hidden = true) User user
    );
    // 특정 시간 혹은 날짜 비활성화
}
