package com.monthlyib.server.domain.videolessons.repository;

import com.monthlyib.server.domain.videolessons.entity.VideoLessons;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoLessonsJpaRepository extends JpaRepository<VideoLessons, Long> {
}
