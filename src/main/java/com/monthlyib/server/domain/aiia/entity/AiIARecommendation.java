package com.monthlyib.server.domain.aiia.entity;

import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "ai_ia_recommendation")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiIARecommendation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String interest;

    private String subject;

    @Column(columnDefinition = "LONGTEXT")
    private String topics;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static AiIARecommendation create(String interest, String subject, String topics, User user) {
        return AiIARecommendation.builder()
                .interest(interest)
                .subject(subject)
                .topics(topics)
                .user(user)
                .build();
    }

    public void updateTopics(String topics) {
        this.topics = topics;
    }
}
