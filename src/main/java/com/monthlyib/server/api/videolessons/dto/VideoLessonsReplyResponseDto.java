package com.monthlyib.server.api.videolessons.dto;

import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.videolessons.entity.VideoLessonsReply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsReplyResponseDto {

    private Long videoLessonsReplyId;

    private Long videoLessonsId;

    private Long authorId;

    private String authorUsername;

    private String authorNickname;

    private String content;

    private List<Long> voteUserId;

    private double star;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public static VideoLessonsReplyResponseDto of(VideoLessonsReply videoLessonsReply) {
        return VideoLessonsReplyResponseDto.builder()
                .videoLessonsReplyId(videoLessonsReply.getVideoLessonsReplyId())
                .videoLessonsId(videoLessonsReply.getVideoLessonsId())
                .authorId(videoLessonsReply.getAuthorId())
                .authorUsername(videoLessonsReply.getAuthorUsername())
                .authorNickname(videoLessonsReply.getAuthorNickname())
                .content(videoLessonsReply.getContent())
                .star(videoLessonsReply.getStar())
                .createAt(videoLessonsReply.getCreateAt())
                .updateAt(videoLessonsReply.getUpdateAt())
                .voteUserId(videoLessonsReply.getVoter().stream().map(User::getUserId).toList())
                .build();
    }

}
