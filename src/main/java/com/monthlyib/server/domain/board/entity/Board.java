package com.monthlyib.server.domain.board.entity;

import com.monthlyib.server.api.board.dto.BoardPatchDto;
import com.monthlyib.server.api.board.dto.BoardPostDto;
import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;


@Setter
@Getter
@Entity
@Table(name = "boards")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;

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

    public static Board create(BoardPostDto dto, Long authorId, String authorUsername, String authorNickName) {
        return Board.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .authorId(authorId)
                .authorUsername(authorUsername)
                .authorNickName(authorNickName)
                .viewCount(0L)
                .build();
    }

    public Board update(BoardPatchDto dto) {
        this.title = Optional.ofNullable(dto.getTitle()).orElse(this.title);
        this.content = Optional.ofNullable(dto.getContent()).orElse(this.content);
        return this;
    }

}
