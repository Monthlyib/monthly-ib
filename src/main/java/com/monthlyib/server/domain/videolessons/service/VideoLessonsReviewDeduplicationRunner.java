package com.monthlyib.server.domain.videolessons.service;

import com.monthlyib.server.api.videolessons.dto.VideoLessonsReviewDeduplicateResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoLessonsReviewDeduplicationRunner implements ApplicationRunner {

    private final VideoLessonsService videoLessonsService;

    @Override
    public void run(ApplicationArguments args) {
        VideoLessonsReviewDeduplicateResponseDto result = videoLessonsService.deduplicateVideoLessonsReviews();
        if (result.getDeletedReviewCount() > 0) {
            log.info(
                    "Video lesson review deduplication completed. scanned={}, duplicateGroups={}, deleted={}, updatedCourses={}",
                    result.getScannedReviewCount(),
                    result.getDuplicateGroupCount(),
                    result.getDeletedReviewCount(),
                    result.getUpdatedVideoLessonsCount()
            );
        }
    }
}
