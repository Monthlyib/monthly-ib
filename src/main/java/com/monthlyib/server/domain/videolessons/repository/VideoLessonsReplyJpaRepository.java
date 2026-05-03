package com.monthlyib.server.domain.videolessons.repository;

import com.monthlyib.server.domain.videolessons.entity.VideoLessonsReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoLessonsReplyJpaRepository extends JpaRepository<VideoLessonsReply, Long> {

    Page<VideoLessonsReply> findAllByVideoLessonsId(Long videoLessonsId, Pageable pageable);

    List<VideoLessonsReply> findAllByVideoLessonsId(Long videoLessonsId);

    boolean existsByVideoLessonsIdAndAuthorId(Long videoLessonsId, Long authorId);

    void deleteAllByVideoLessonsId(Long videoLessonId);

    long countAllByVideoLessonsId(Long videoLessonId);

}
