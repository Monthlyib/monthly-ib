package com.monthlyib.server.utils;

import com.monthlyib.server.api.board.dto.BoardFileResponseDto;
import com.monthlyib.server.api.board.dto.BoardReplyResponseDto;
import com.monthlyib.server.api.board.dto.BoardResponseDto;
import com.monthlyib.server.api.board.dto.BoardSimpleResponseDto;
import com.monthlyib.server.api.monthlyib.dto.MonthlyIbResponseDto;
import com.monthlyib.server.api.news.dto.NewsFileResponseDto;
import com.monthlyib.server.api.news.dto.NewsResponseDto;
import com.monthlyib.server.api.news.dto.NewsSimpleResponseDto;
import com.monthlyib.server.api.question.dto.AnswerResponseDto;
import com.monthlyib.server.api.question.dto.QuestionResponseDto;
import com.monthlyib.server.api.question.dto.QuestionSimpleResponseDto;
import com.monthlyib.server.api.tutoring.dto.TutoringDetailResponseDto;
import com.monthlyib.server.api.tutoring.dto.TutoringRemainDto;
import com.monthlyib.server.api.tutoring.dto.TutoringResponseDto;
import com.monthlyib.server.api.tutoring.dto.TutoringSimpleResponseDto;
import com.monthlyib.server.api.user.dto.UserResponseDto;
import com.monthlyib.server.api.videolessons.dto.*;
import com.monthlyib.server.constant.*;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class StubUtils {


    public Page<QuestionResponseDto> getQuestionPage() {

        return new PageImpl(
                List.of(getQuestionSimpleResponse(), getQuestionSimpleResponse()),
                PageRequest.of(0, 10),
                2
        );
    }

    public QuestionSimpleResponseDto getQuestionSimpleResponse() {
        return QuestionSimpleResponseDto.builder()
                .questionId(1L)
                .title("Question Title")
                .subject("Question Subject")
                .authorId(1L)
                .authorUsername("Author Username")
                .authorNickName("Author Nickname")
                .questionStatus(QuestionStatus.ANSWER_WAIT)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

    public QuestionResponseDto getQuestionResponse() {
        return QuestionResponseDto.builder()
                .questionId(1L)
                .title("Question Title")
                .content("Question Content")
                .subject("Question Subject")
                .authorId(1L)
                .authorUsername("Author Username")
                .authorNickName("Author Nickname")
                .questionStatus(QuestionStatus.ANSWER_WAIT)
                .answer(getAnswerResponse())
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

    public AnswerResponseDto getAnswerResponse() {
        return AnswerResponseDto.builder()
                .answerId(1L)
                .questionId(1L)
                .content("Answer Content")
                .authorId(1L)
                .authorUsername("Author Username")
                .authorNickName("Author Nickname")
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

    public Page<VideoLessonsSimpleResponseDto> getVideoLessons() {

        return new PageImpl(
                List.of(getVideoLessonsSimpleResponseDto(), getVideoLessonsSimpleResponseDto()),
                PageRequest.of(0, 10),
                2
        );
    }




    public VideoLessonsReplyResponseDto getVideoLessonsReplyResponse() {
        return VideoLessonsReplyResponseDto.builder()
                .videoLessonsReplyId(1L)
                .videoLessonsId(1L)
                .authorId(1L)
                .authorUsername("Username")
                .authorNickname("Nickname")
                .content("Content 1")
                .voteUserId(List.of(1L, 2L, 3L))
                .star(4.5)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

    public VideoLessonsChapterResponseDto getVideoLessonsChapterResponseOne() {
        return VideoLessonsChapterResponseDto.builder()
                .chapterId(1L)
                .chapterStatus(VideoChapterStatus.MAIN_CHAPTER)
                .chapterTitle("Main Chapter 1")
                .chapterIndex(1)
                .subChapters(List.of(getVideoLessonsSubChapterResponseOne(), getVideoLessonsSubChapterResponseTwo()))
                .build();
    }

    public VideoLessonsChapterResponseDto getVideoLessonsChapterResponseTwo() {
        return VideoLessonsChapterResponseDto.builder()
                .chapterId(2L)
                .chapterStatus(VideoChapterStatus.MAIN_CHAPTER)
                .chapterTitle("Main Chapter 2")
                .chapterIndex(2)
                .subChapters(List.of(getVideoLessonsSubChapterResponseSubOne(), getVideoLessonsSubChapterResponseSubTwo()))
                .build();
    }
    public VideoLessonsSubChapterResponseDto getVideoLessonsSubChapterResponseOne() {
        return VideoLessonsSubChapterResponseDto.builder()
                .chapterId(2L)
                .mainChapterId(1L)
                .chapterStatus(VideoChapterStatus.SUB_CHAPTER)
                .chapterTitle("Sub Chapter 1")
                .chapterIndex(1)
                .videoFileUrl("Video File URL")
                .build();
    }
    public VideoLessonsSubChapterResponseDto getVideoLessonsSubChapterResponseTwo() {
        return VideoLessonsSubChapterResponseDto.builder()
                .chapterId(3L)
                .mainChapterId(1L)
                .chapterStatus(VideoChapterStatus.SUB_CHAPTER)
                .chapterTitle("Sub Chapter 2")
                .chapterIndex(2)
                .videoFileUrl("Video File URL")
                .build();
    }
    public VideoLessonsSubChapterResponseDto getVideoLessonsSubChapterResponseSubOne() {
        return VideoLessonsSubChapterResponseDto.builder()
                .chapterId(4L)
                .mainChapterId(2L)
                .chapterStatus(VideoChapterStatus.SUB_CHAPTER)
                .chapterTitle("Sub Chapter 1")
                .chapterIndex(1)
                .videoFileUrl("Video File URL")
                .build();
    }
    public VideoLessonsSubChapterResponseDto getVideoLessonsSubChapterResponseSubTwo() {
        return VideoLessonsSubChapterResponseDto.builder()
                .chapterId(5L)
                .mainChapterId(2L)
                .chapterStatus(VideoChapterStatus.SUB_CHAPTER)
                .chapterTitle("Sub Chapter 2")
                .chapterIndex(2)
                .videoFileUrl("Video File URL")
                .build();
    }

    public VideoLessonsSimpleResponseDto getVideoLessonsSimpleResponseDto() {
        return VideoLessonsSimpleResponseDto.builder()
                .videoLessonsId(1L)
                .title("Video Lesson 1")
                .content("Video Lesson 1")
                .firstCategory(getFirstCategory())
                .secondCategory(getSecondCategory())
                .thirdCategory(getThirdCategory())
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

    public VideoCategoryResponseDto getFirstCategory() {
        return VideoCategoryResponseDto.builder()
                .videoCategoryId(1L)
                .videoCategoryStatus(VideoCategoryStatus.FIRST_CATEGORY)
                .categoryName("First Category")
                .build();
    }

    public VideoCategoryResponseDto getSecondCategory() {
        return VideoCategoryResponseDto.builder()
                .videoCategoryId(1L)
                .videoCategoryStatus(VideoCategoryStatus.SECOND_CATEGORY)
                .categoryName("Second Category")
                .build();
    }
    public VideoCategoryResponseDto getThirdCategory() {
        return VideoCategoryResponseDto.builder()
                .videoCategoryId(1L)
                .videoCategoryStatus(VideoCategoryStatus.THIRD_CATEGORY)
                .categoryName("Third Category")
                .build();
    }

    public Page<UserResponseDto> getUserPageResponse() {

        return new PageImpl(
                List.of(getUserResponseDto(), getUserResponseDto()),
                PageRequest.of(0, 10),
                2
        );
    }

    public UserResponseDto getUserResponseDto() {
        return UserResponseDto.builder()
                .userId(1L)
                .username("test")
                .email("test@test.com")
                .nickName("홍길동")
                .birth("000101")
                .school("ㅇㅇ고등학교")
                .grade("2")
                .address("주소")
                .memo("메모")
                .country("한국")
                .termsOfUseCheck(true)
                .privacyTermsCheck(true)
                .marketingTermsCheck(true)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }
    public Page<NewsSimpleResponseDto> getSimplePageResponse() {

        return new PageImpl(
                List.of(getNewsSimpleResponseDto(), getNewsSimpleResponseDto()),
                PageRequest.of(0, 10),
                2
        );
    }

    public NewsSimpleResponseDto getNewsSimpleResponseDto() {
        return NewsSimpleResponseDto.builder()
                .newsId(1L)
                .authorUserId(1L)
                .authorUsername("test")
                .authorNickName("test")
                .title("title")
                .content("content")
                .viewCount(1234)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

    public NewsResponseDto getNewsResponseDto() {
        return NewsResponseDto.builder()
                .newsId(1L)
                .authorUserId(1L)
                .authorUsername("test")
                .authorNickName("test")
                .title("title")
                .content("content")
                .viewCount(1234)
                .files(List.of(getNewsFileResponseDto(), getNewsFileResponseDto()))
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

    public NewsFileResponseDto getNewsFileResponseDto() {
        return NewsFileResponseDto.builder()
                .fileId(1L)
                .fileName("file")
                .fileUrl("File URL")
                .build();
    }


    public Page<BoardSimpleResponseDto> getBoardSimplePageResponse() {

        return new PageImpl(
                List.of(getBoardSimpleResponseDto(), getBoardSimpleResponseDto()),
                PageRequest.of(0, 10),
                2
        );
    }
    public BoardSimpleResponseDto getBoardSimpleResponseDto() {
        return BoardSimpleResponseDto.builder()
                .boardId(1L)
                .authorUserId(1L)
                .authorUsername("test")
                .authorNickName("test")
                .title("title")
                .content("content")
                .viewCount(1234)
                .replyCount(21)
                .fileCount(2)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

    public BoardResponseDto getBoardResponseDto() {
        List<BoardReplyResponseDto> list = List.of(getBoardReplyResponseDto(), getBoardReplyResponseDto());
        PageImpl page = new PageImpl(list, PageRequest.of(0, 10), 2);
        return BoardResponseDto.builder()
                .boardId(1L)
                .authorUserId(1L)
                .authorUsername("test")
                .authorNickName("test")
                .title("title")
                .content("content")
                .viewCount(1234)
                .reply(PageResponseDto.of(page, page.getContent(), Result.ok()))
                .files(List.of(getBoardFileResponseDto(), getBoardFileResponseDto()))
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

    public BoardReplyResponseDto getBoardReplyResponseDto() {
        return BoardReplyResponseDto.builder()
                .boardReplyId(1L)
                .boardId(1L)
                .authorId(1L)
                .authorUsername("test")
                .authorNickname("test")
                .content("test reply")
                .voteUserId(List.of(1L, 2L, 3L, 4L))
                .voterCount(4)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

    public BoardFileResponseDto getBoardFileResponseDto() {
        return BoardFileResponseDto.builder()
                .fileId(1L)
                .fileName("file")
                .fileUrl("File URL")
                .build();
    }

    public Page<MonthlyIbResponseDto> getMonthlyIbPageResponse() {

        return new PageImpl(
                List.of(getMonthlyIbResponseDto(), getMonthlyIbResponseDto()),
                PageRequest.of(0, 10),
                2
        );
    }


    public MonthlyIbResponseDto getMonthlyIbResponseDto() {
        return MonthlyIbResponseDto.builder()
                .build();
    }

    public TutoringSimpleResponseDto getTutoringSimpleResponseDto(LocalDate localDate) {
        return TutoringSimpleResponseDto.builder()
                .date(localDate)
                .currentTutoring(
                        List.of(
                                getTutoringRemainDto(10,30),
                                getTutoringRemainDto(15,30),
                                getTutoringRemainDto(17, 00),
                                getFullTutoringRemainDto(20, 30),
                                getFullTutoringRemainDto(21,00),
                                getFullTutoringRemainDto(21,30)
                        )
                )
                .build();
    }

    public TutoringRemainDto getTutoringRemainDto(int hour, int minute) {
        return TutoringRemainDto.builder()
                .hour(hour)
                .minute(minute)
                .remainTutoring(1)
                .totalTutoring(3)
                .tutoringList(List.of(1L))
                .build();
    }

    public TutoringRemainDto getFullTutoringRemainDto(int hour, int minute) {
        return TutoringRemainDto.builder()
                .hour(hour)
                .minute(minute)
                .remainTutoring(3)
                .totalTutoring(3)
                .tutoringList(List.of(4L,5L,7L))
                .build();
    }



    public TutoringResponseDto getTutoringResponseDto(LocalDate localDate, int hour, int minute) {
        return TutoringResponseDto.builder()
                .tutoringId(1L)
                .date(localDate)
                .hour(hour)
                .minute(minute)
                .requestUserId(1L)
                .requestUsername("신청인")
                .requestUserNickName("신청인")
                .detail("신청내용")
                .tutoringStatus(TutoringStatus.WAIT)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }



    public Page<TutoringResponseDto> getTutoringPageResponseDto() {

        return new PageImpl(
                List.of(
                        getTutoringResponseDto(LocalDate.now(), 10, 30),
                        getTutoringResponseDto(LocalDate.now(), 11, 30),
                        getTutoringResponseDto(LocalDate.now(), 12, 00),
                        getTutoringResponseDto(LocalDate.now(), 12, 30),
                        getTutoringResponseDto(LocalDate.now(), 12, 30),
                        getTutoringResponseDto(LocalDate.now(), 20, 00),
                        getTutoringResponseDto(LocalDate.now(), 20, 30),
                        getTutoringResponseDto(LocalDate.now(), 20, 30),
                        getTutoringResponseDto(LocalDate.now(), 21, 30),
                        getTutoringResponseDto(LocalDate.now(), 21, 30),
                        getTutoringResponseDto(LocalDate.now(), 21, 30)
                ),
                PageRequest.of(0, 10),
                2
        );
    }

}
