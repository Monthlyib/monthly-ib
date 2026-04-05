package com.monthlyib.server.domain.headernavigation.entity;

import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "header_navigation_page")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeaderNavigationPage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String pageKey;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String configJson;

    @Column(nullable = true)
    private Long updatedBy;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static HeaderNavigationPage create(String pageKey, String configJson, Long userId) {
        return HeaderNavigationPage.builder()
                .pageKey(pageKey)
                .configJson(configJson)
                .updatedBy(userId)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void updateConfig(String configJson, Long userId) {
        this.configJson = configJson;
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }
}
