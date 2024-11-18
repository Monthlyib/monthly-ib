package com.monthlyib.server.domain.board.entity;

import com.monthlyib.server.api.board.dto.BoardReplyPostDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "board_replys")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardReply extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardReplyId;

    @Column(nullable = false)
    private Long boardId;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;


    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String authorUsername;

    @Column(nullable = false)
    private String authorNickName;

    @ManyToMany
    Set<User> voter = new LinkedHashSet<>();

    public static BoardReply create(BoardReplyPostDto dto, Long boardId, Long authorId, String authorUsername, String authorNickName) {
        return BoardReply.builder()
                .boardId(boardId)
                .authorId(authorId)
                .authorUsername(authorUsername)
                .authorNickName(authorNickName)
                .content(dto.getContent())
                .voter(new LinkedHashSet<>())
                .build();
    }
}
