package com.monthlyib.server.domain.videolessons.entity;


import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.board.entity.BoardFile;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "video_lessons_thumbnail")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoThumbnail extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoThumbnailId;

    @Column(nullable = false)
    private Long videoLessonsId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String url;

    public static VideoThumbnail create(Long videoLessonsId, String fileName, String url) {
        return VideoThumbnail.builder()
                .videoLessonsId(videoLessonsId)
                .fileName(fileName)
                .url(url)
                .build();
    }

}
