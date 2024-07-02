package com.monthlyib.server.domain.montlyib.entity;

import com.monthlyib.server.api.monthlyib.dto.MonthlyIbPostDto;
import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "monthly_ib")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyIb extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long monthlyIbId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private Long monthlyIbThumbnailFileId;

    @Column(nullable = false)
    private String monthlyIbThumbnailFileName;

    @Column(nullable = false)
    private String monthlyIbThumbnailFileUrl;

    public static MonthlyIb create(MonthlyIbPostDto dto) {
        return MonthlyIb.builder()
                .title(dto.getTitle())
                .content(dto.getTitle())
                .monthlyIbThumbnailFileId(0L)
                .monthlyIbThumbnailFileName("")
                .monthlyIbThumbnailFileUrl("")
                .build();
    }


}
