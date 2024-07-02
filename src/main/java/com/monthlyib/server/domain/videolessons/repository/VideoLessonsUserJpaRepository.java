package com.monthlyib.server.domain.videolessons.repository;

import com.monthlyib.server.domain.videolessons.entity.VideoLessonsUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoLessonsUserJpaRepository extends JpaRepository<VideoLessonsUser, Long> {

    Optional<VideoLessonsUser> findByUserIdAndVideoLessonsId(Long userId, Long videoLessonsId);

    Page<VideoLessonsUser> findAllByUserId(Long userId, Pageable pageable);
}
