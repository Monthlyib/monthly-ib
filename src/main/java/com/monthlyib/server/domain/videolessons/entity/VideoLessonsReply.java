package com.monthlyib.server.domain.videolessons.entity;


import com.monthlyib.server.api.videolessons.dto.VideoLessonsReplyPatchDto;
import com.monthlyib.server.api.videolessons.dto.VideoLessonsReplyPostDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.VideoCategoryStatus;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "video_lessons_reply")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsReply extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoLessonsReplyId;

    @Column(nullable = false)
    private Long videoLessonsId;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String authorUsername;

    @Column(nullable = false)
    private String authorNickname;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private double star;

    @ManyToMany
    Set<User> voter = new LinkedHashSet<>();

    public static VideoLessonsReply create(VideoLessonsReplyPostDto dto, User user) {
        return VideoLessonsReply.builder()
                .videoLessonsId(dto.getVideoLessonsId())
                .authorId(dto.getAuthorId())
                .authorUsername(user.getUsername())
                .authorNickname(user.getNickName())
                .content(dto.getContent())
                .star(dto.getStar())
                .voter(new LinkedHashSet<>())
                .build();
    }

    public VideoLessonsReply update(VideoLessonsReplyPatchDto dto) {
        this.star = dto.getStar();
        this.content = Optional.ofNullable(dto.getContent()).orElse(this.content);
        return this;
    }


}
