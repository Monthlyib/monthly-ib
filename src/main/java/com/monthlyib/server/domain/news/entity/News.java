package com.monthlyib.server.domain.news.entity;


import com.monthlyib.server.api.news.dto.NewsPostDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "news")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class News extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long newsId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String authorUsername;

    @Column(nullable = false)
    private String authorNickName;

    @Column(nullable = false)
    private long viewCount;

    public static News create(NewsPostDto newsPostDto, User user) {
        return News.builder()
                .title(newsPostDto.getTitle())
                .content(newsPostDto.getContent())
                .authorId(user.getUserId())
                .authorUsername(user.getUsername())
                .authorNickName(user.getNickName())
                .viewCount(0L)
                .build();
    }

}
