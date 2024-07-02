package com.monthlyib.server.domain.board.entity;

import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "board_files")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardFile extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardFileId;

    @Column(nullable = false)
    private Long boardId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String url;

    public static BoardFile create(Long boardId, String fileName, String url) {
        return BoardFile.builder()
                .boardId(boardId)
                .fileName(fileName)
                .url(url)
                .build();
    }

}
