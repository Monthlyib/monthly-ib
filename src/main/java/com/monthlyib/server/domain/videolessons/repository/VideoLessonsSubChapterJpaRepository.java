package com.monthlyib.server.domain.videolessons.repository;

import com.monthlyib.server.domain.videolessons.entity.VideoLessonsSubChapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoLessonsSubChapterJpaRepository extends JpaRepository<VideoLessonsSubChapter, Long> {

    List<VideoLessonsSubChapter> findAllByVideoLessonsId(Long videoLessonId);

    List<VideoLessonsSubChapter> findAllByMainChapterId(Long mainChapterId);

    void deleteAllByVideoLessonsId(Long videoLessonId);
}
