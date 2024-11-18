package com.monthlyib.server.domain.videolessons.entity;


import com.monthlyib.server.api.videolessons.dto.VideoCategoryPatchDto;
import com.monthlyib.server.api.videolessons.dto.VideoCategoryPostDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.VideoCategoryStatus;
import com.monthlyib.server.constant.VideoChapterStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "video_lessons_category")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsCategory extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoCategoryId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VideoCategoryStatus videoCategoryStatus;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false)
    private Long parentsCategoryId;


    public static VideoLessonsCategory create(VideoCategoryPostDto dto) {
        return VideoLessonsCategory.builder()
                .videoCategoryStatus(dto.getVideoCategoryStatus())
                .categoryName(dto.getCategoryName())
                .parentsCategoryId(Optional.ofNullable(dto.getParentsId()).orElse(0L))
                .build();
    }

    public VideoLessonsCategory update(VideoCategoryPatchDto dto) {
        this.videoCategoryStatus = Optional.ofNullable(dto.getVideoCategoryStatus()).orElse(this.videoCategoryStatus);
        this.categoryName = Optional.ofNullable(dto.getCategoryName()).orElse(this.categoryName);
        this.parentsCategoryId = Optional.ofNullable(dto.getParentsId()).orElse(this.parentsCategoryId);
        return this;
    }




}
