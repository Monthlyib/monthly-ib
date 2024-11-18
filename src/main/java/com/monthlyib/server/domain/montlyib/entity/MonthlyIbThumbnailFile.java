package com.monthlyib.server.domain.montlyib.entity;


import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "monthly_ib_thumbnail")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyIbThumbnailFile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long monthlyIbThumbnailFileId;

    @Column(nullable = false, unique = true)
    private Long monthlyIbId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String url;

    public static MonthlyIbThumbnailFile create(String url, String fileName, Long monthlyIbId) {
        return MonthlyIbThumbnailFile.builder()
                .monthlyIbId(monthlyIbId)
                .fileName(fileName)
                .url(url)
                .build();
    }
}
