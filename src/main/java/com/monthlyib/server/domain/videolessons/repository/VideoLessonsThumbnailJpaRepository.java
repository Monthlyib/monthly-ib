package com.monthlyib.server.domain.videolessons.repository;

import com.monthlyib.server.domain.videolessons.entity.VideoThumbnail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoLessonsThumbnailJpaRepository extends JpaRepository<VideoThumbnail, Long> {

    List<VideoThumbnail> findAllByVideoLessonsId(Long videoLessonsId);

    void deleteAllByVideoLessonsId(Long videoLessonsId);
}
