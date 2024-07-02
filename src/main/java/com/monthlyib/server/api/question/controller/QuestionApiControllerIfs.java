package com.monthlyib.server.api.question.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.question.dto.*;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "H. Question", description = "질문하기 관련 API")
public interface QuestionApiControllerIfs {

    class QuestionListResponse extends PageResponseDto<List<QuestionSimpleResponseDto>> { }

    @Operation(summary = "전체 질문 Data 요청(개인, 관리자)", description = "전체 질문 Data 리스트 요청(개인이 요청할 경우 본인의 질문만 응답)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = QuestionListResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<PageResponseDto<?>> getQuestionList(
            @RequestParam(defaultValue = "0") int page,
            @ModelAttribute QuestionSearchDto requestDto
    );

    class QuestionResponse extends ResponseDto<QuestionResponseDto> { }
    @Operation(summary = "질문 단건 조회(개인, 관리자)", description = "질문 단건 조회 요청(개인이 요청할시 본인 질문 아니면 403)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = QuestionResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getQuestion(
            @PathVariable @Parameter(description = "Question 식별자", required = true) Long questionId
    );

    @Operation(summary = "특정 회원 전체 질문 Data 요청(개인, 관리자)", description = "특정 회원전체 질문 Data 리스트 요청(개인이 요청할 경우 본인의 질문만 응답)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = QuestionListResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<PageResponseDto<?>> getMyQuestionList(
            @ModelAttribute QuestionSearchDto requestDto,
            @RequestParam(defaultValue = "0") int page,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "질문 데이터 생성(개인, 관리자)", description = "질문 데이터 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = QuestionResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postQuestion(
            @RequestBody QuestionPostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "질문 수정(개인, 관리자)", description = "질문 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = QuestionResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchQuestion(
            @RequestBody QuestionPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "질문 삭제(개인, 관리자)", description = "질문 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteQuestion(
            @PathVariable @Parameter(description = "Question 식별자", required = true) Long questionId,
            @UserSession @Parameter(hidden = true) User user
    );


    @Operation(summary = "질문 답변 데이터 생성(관리자)", description = "질문 답변 데이터 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postQuestionAnswer(
            @RequestBody AnswerPostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "질문 답변 수정(관리자)", description = "질문 답변 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchQuestionAnswer(
            @RequestBody AnswerPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );


    @Operation(summary = "질문 답변 삭제(관리자)", description = "질문 답변 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteAnswer(
            @PathVariable @Parameter(description = "Answer 식별자", required = true) Long answerId,
            @UserSession @Parameter(hidden = true) User user
    );



}
