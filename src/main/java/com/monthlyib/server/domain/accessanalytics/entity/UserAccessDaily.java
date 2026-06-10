package com.monthlyib.server.domain.accessanalytics.entity;

import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_access_daily",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_access_daily_date_user",
                        columnNames = {"access_date", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_access_daily_date", columnList = "access_date"),
                @Index(name = "idx_user_access_daily_user", columnList = "user_id")
        }
)
public class UserAccessDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userAccessDailyId;

    @Column(name = "access_date", nullable = false)
    private LocalDate accessDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime firstAccessAt;

    @Column(nullable = false)
    private LocalDateTime lastAccessAt;

    @Column(nullable = false)
    private Long accessCount;
}
