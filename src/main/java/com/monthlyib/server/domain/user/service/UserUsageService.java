package com.monthlyib.server.domain.user.service;

import com.monthlyib.server.api.subscribe.dto.SubscribeUserResponseDto;
import com.monthlyib.server.api.user.dto.UserUsageCourseDto;
import com.monthlyib.server.api.user.dto.UserUsageResponseDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.QuestionStatus;
import com.monthlyib.server.constant.TutoringStatus;
import com.monthlyib.server.domain.question.repository.QuestionRepository;
import com.monthlyib.server.domain.subscribe.service.SubscribeService;
import com.monthlyib.server.domain.tutoring.repository.TutoringRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.repository.UserRepository;
import com.monthlyib.server.domain.videolessons.entity.VideoLessons;
import com.monthlyib.server.domain.videolessons.entity.VideoLessonsProgress;
import com.monthlyib.server.domain.videolessons.entity.VideoLessonsUser;
import com.monthlyib.server.domain.videolessons.repository.VideoLessonsRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserUsageService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final TutoringRepository tutoringRepository;
    private final VideoLessonsRepository videoLessonsRepository;
    private final SubscribeService subscribeService;

    public UserUsageResponseDto findUsage(Long userId, User adminUser) {
        verifyAdmin(adminUser);

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));

        long totalQuestionCount = questionRepository.countByAuthorId(userId);
        long waitingQuestionCount = questionRepository.countByAuthorIdAndStatus(userId, QuestionStatus.ANSWER_WAIT);
        long completedQuestionCount = questionRepository.countByAuthorIdAndStatus(userId, QuestionStatus.COMPLETE);

        long totalTutoringCount = tutoringRepository.countByRequestUserId(userId);
        long waitingTutoringCount = tutoringRepository.countByRequestUserIdAndStatus(userId, TutoringStatus.WAIT);
        long confirmedTutoringCount = tutoringRepository.countByRequestUserIdAndStatus(userId, TutoringStatus.CONFIRM);
        long canceledTutoringCount = tutoringRepository.countByRequestUserIdAndStatus(userId, TutoringStatus.CANCEL);

        List<UserUsageCourseDto> courseUsageList = videoLessonsRepository.findAllVideoLessonsUser(userId).stream()
                .map(videoLessonsUser -> buildCourseUsage(userId, videoLessonsUser))
                .sorted(
                        Comparator.comparing(
                                UserUsageCourseDto::getLastWatchedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        ).thenComparing(
                                UserUsageCourseDto::getEnrolledAt,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                )
                .toList();

        SubscribeUserResponseDto activeSubscribe = subscribeService.findActiveSubscribeUser(userId, adminUser);

        return UserUsageResponseDto.builder()
                .userId(targetUser.getUserId())
                .username(targetUser.getUsername())
                .nickName(targetUser.getNickName())
                .lastAccessAt(targetUser.getLastAccessAt())
                .activeSubscribe(activeSubscribe)
                .totalQuestionCount(totalQuestionCount)
                .waitingQuestionCount(waitingQuestionCount)
                .completedQuestionCount(completedQuestionCount)
                .totalTutoringCount(totalTutoringCount)
                .waitingTutoringCount(waitingTutoringCount)
                .confirmedTutoringCount(confirmedTutoringCount)
                .canceledTutoringCount(canceledTutoringCount)
                .totalCourseCount(courseUsageList.size())
                .courses(courseUsageList)
                .build();
    }

    private UserUsageCourseDto buildCourseUsage(Long userId, VideoLessonsUser videoLessonsUser) {
        Long videoLessonsId = videoLessonsUser.getVideoLessonsId();
        VideoLessons videoLessons = videoLessonsRepository.findVideoById(videoLessonsId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_VIDEO_LESSONS));
        List<VideoLessonsProgress> progressList = videoLessonsRepository.findAllVideoLessonsProgress(userId, videoLessonsId);
        long totalLessonCount = videoLessonsRepository.findVideoSubChaptersByVideoLessonsId(videoLessonsId).size();
        long completedLessonCount = progressList.stream().filter(VideoLessonsProgress::isCompleted).count();
        double averageProgress = totalLessonCount == 0
                ? 0D
                : progressList.stream()
                        .mapToDouble(progress -> Optional.ofNullable(progress.getProgressPercent()).orElse(0D))
                        .sum() / totalLessonCount;
        LocalDateTime lastWatchedAt = progressList.stream()
                .map(VideoLessonsProgress::getLastWatchedAt)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return UserUsageCourseDto.builder()
                .videoLessonsId(videoLessonsId)
                .title(videoLessons.getTitle())
                .progressPercent(Math.round(averageProgress * 10D) / 10D)
                .completedLessonCount(completedLessonCount)
                .totalLessonCount(totalLessonCount)
                .enrolledAt(videoLessonsUser.getCreateAt())
                .lastWatchedAt(lastWatchedAt)
                .build();
    }

    private void verifyAdmin(User user) {
        if (user == null || !Authority.ADMIN.equals(user.getAuthority())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }
}
