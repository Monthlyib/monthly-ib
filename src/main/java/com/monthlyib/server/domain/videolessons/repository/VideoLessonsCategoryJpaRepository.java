package com.monthlyib.server.domain.videolessons.repository;

import com.monthlyib.server.constant.VideoCategoryStatus;
import com.monthlyib.server.domain.videolessons.entity.VideoLessonsCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoLessonsCategoryJpaRepository extends JpaRepository<VideoLessonsCategory, Long> {

    List<VideoLessonsCategory> findAllByVideoCategoryStatus(VideoCategoryStatus videoCategoryStatus);
}
