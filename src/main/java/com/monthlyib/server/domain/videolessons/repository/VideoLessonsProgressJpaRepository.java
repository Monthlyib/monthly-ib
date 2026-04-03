package com.monthlyib.server.domain.videolessons.repository;

import com.monthlyib.server.domain.videolessons.entity.VideoLessonsProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoLessonsProgressJpaRepository extends JpaRepository<VideoLessonsProgress, Long> {

    Optional<VideoLessonsProgress> findByUserIdAndVideoLessonsIdAndSubChapterId(
            Long userId,
            Long videoLessonsId,
            Long subChapterId
    );

    List<VideoLessonsProgress> findAllByUserIdAndVideoLessonsId(Long userId, Long videoLessonsId);
}
