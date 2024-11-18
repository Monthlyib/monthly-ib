package com.monthlyib.server.domain.montlyib.entity;


import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "monthly_ib_pdf")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyIbPdfFile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long monthlyIbPdfFileId;

    @Column(nullable = false)
    private Long monthlyIbId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String url;

    public static MonthlyIbPdfFile create(Long monthlyIbId,String fileName, String url) {
        return MonthlyIbPdfFile.builder()
                .fileName(fileName)
                .url(url)
                .monthlyIbId(monthlyIbId)
                .build();
    }
}
