package com.monthlyib.server.domain.videolessons.repository;

import com.monthlyib.server.domain.videolessons.entity.VideoLessonsMainChapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoLessonsMainChapterJpaRepository extends JpaRepository<VideoLessonsMainChapter, Long> {

    List<VideoLessonsMainChapter> findAllByVideoLessonsId(Long videoLessonsId);

    void deleteAllByVideoLessonsId(Long videoLessonsId);
}
