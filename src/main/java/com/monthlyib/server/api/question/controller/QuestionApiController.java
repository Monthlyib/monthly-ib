package com.monthlyib.server.api.question.controller;


import com.monthlyib.server.api.question.dto.*;
import com.monthlyib.server.domain.question.service.QuestionService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.utils.StubUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class QuestionApiController implements QuestionApiControllerIfs{


    private final QuestionService questionService;

    @Override
    @GetMapping("/open-api/question")
    public ResponseEntity<PageResponseDto<?>> getQuestionList(int page, QuestionSearchDto requestDto) {
        Page<QuestionResponseDto> response = questionService.findAllQuestion(page, requestDto);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @GetMapping("/open-api/question/{questionId}")
    public ResponseEntity<ResponseDto<?>> getQuestion(Long questionId) {
        QuestionResponseDto response = questionService.findQuestion(questionId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @GetMapping("/api/question")
    public ResponseEntity<PageResponseDto<?>> getMyQuestionList(QuestionSearchDto requestDto, int page, User user) {
        Page<QuestionResponseDto> response = questionService.findAllQuestionByUserId(page, requestDto, user.getUserId());
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @PostMapping("/api/question")
    public ResponseEntity<ResponseDto<?>> postQuestion(QuestionPostDto requestDto, User user) {
        QuestionResponseDto response = questionService.createQuestion(requestDto, user.getUserId());
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/question")
    public ResponseEntity<ResponseDto<?>> patchQuestion(QuestionPatchDto requestDto, User user) {
        QuestionResponseDto response = questionService.updateQuestion(requestDto, user.getUserId());
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/question/{questionId}")
    public ResponseEntity<ResponseDto<?>> deleteQuestion(Long questionId, User user) {
        questionService.deleteQuestion(questionId, user.getUserId());
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @PostMapping("/api/question/answer")
    public ResponseEntity<ResponseDto<?>> postQuestionAnswer(AnswerPostDto requestDto, User user) {
        questionService.createAnswer(requestDto, user.getUserId());
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @PatchMapping("/api/question/answer")
    public ResponseEntity<ResponseDto<?>> patchQuestionAnswer(AnswerPatchDto requestDto, User user) {
        questionService.updateAnswer(requestDto, user.getUserId());
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @DeleteMapping("/api/question/answer/{answerId}")
    public ResponseEntity<ResponseDto<?>> deleteAnswer(Long answerId, User user) {
        questionService.deleteAnswer(answerId, user.getUserId());
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }
}
