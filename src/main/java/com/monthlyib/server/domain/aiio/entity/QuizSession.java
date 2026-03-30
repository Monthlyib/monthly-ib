package com.monthlyib.server.domain.aiio.entity;

import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "quiz_session")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSession extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chapter;

    private Integer durationMinutes;

    @Builder.Default
    private boolean isSubmitted = false;

    private LocalDateTime startedAt;

    private String subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static QuizSession create(String subject, String chapter, int durationMinutes, User user) {
        return QuizSession.builder()
                .subject(subject)
                .chapter(chapter)
                .durationMinutes(durationMinutes)
                .isSubmitted(false)
                .startedAt(LocalDateTime.now())
                .user(user)
                .build();
    }
}
