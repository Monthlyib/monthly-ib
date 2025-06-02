package com.monthlyib.server.domain.aiio.entity;

import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSession extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String chapter;

    private int durationMinutes;

    private LocalDateTime startedAt;

    private boolean isSubmitted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
