package com.monthlyib.server.domain.user.entity;

import com.monthlyib.server.constant.LoginType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_login_provider",
        indexes = {
                @Index(name = "idx_user_login_provider_user", columnList = "user_id"),
                @Index(name = "idx_user_login_provider_provider_email", columnList = "provider,provider_email")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_login_provider_provider_email",
                        columnNames = {"provider", "provider_email"}
                )
        }
)
public class UserLoginProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userLoginProviderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoginType provider;

    @Column(name = "provider_email", nullable = false, length = 255)
    private String providerEmail;

    @Column(nullable = false)
    private LocalDateTime linkedAt;

    public static UserLoginProvider create(User user, LoginType provider, String providerEmail) {
        return UserLoginProvider.builder()
                .user(user)
                .provider(provider)
                .providerEmail(providerEmail)
                .linkedAt(LocalDateTime.now())
                .build();
    }
}
