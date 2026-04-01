package com.monthlyib.server.domain.home.entity;

import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "home_layout_page")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeLayoutPage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String pageKey;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String draftJson;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String publishedJson;

    @Column(nullable = true)
    private Long draftUpdatedBy;

    @Column(nullable = true)
    private Long publishedBy;

    @Column(nullable = false)
    private LocalDateTime draftUpdatedAt;

    @Column(nullable = true)
    private LocalDateTime publishedAt;

    public static HomeLayoutPage createDefault(String pageKey, String defaultJson) {
        LocalDateTime now = LocalDateTime.now();
        return HomeLayoutPage.builder()
                .pageKey(pageKey)
                .draftJson(defaultJson)
                .publishedJson(defaultJson)
                .draftUpdatedAt(now)
                .publishedAt(now)
                .build();
    }

    public void updateDraft(String json, Long userId) {
        this.draftJson = json;
        this.draftUpdatedBy = userId;
        this.draftUpdatedAt = LocalDateTime.now();
    }

    public void publish(Long userId) {
        this.publishedJson = this.draftJson;
        this.publishedBy = userId;
        this.publishedAt = LocalDateTime.now();
    }

    public void resetDraft(Long userId) {
        this.draftJson = this.publishedJson;
        this.draftUpdatedBy = userId;
        this.draftUpdatedAt = LocalDateTime.now();
    }
}
