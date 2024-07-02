package com.monthlyib.server.domain.news.entity;

import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.board.entity.BoardFile;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "news_file")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsFile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long newsFileId;

    @Column(nullable = false)
    private Long newsId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String url;

    public static NewsFile create(Long newsId, String fileName, String url) {
        return NewsFile.builder()
                .newsId(newsId)
                .fileName(fileName)
                .url(url)
                .build();
    }
}
