package com.monthlyib.server.domain.user.entity;

import com.monthlyib.server.api.subscribe.dto.SubscribePostDto;
import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "user_image")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userImageId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private Long userId;

    public static UserImage create(String fileName, String fileUrl, Long userId) {
        return UserImage.builder()
                .fileName(fileName)
                .fileUrl(fileUrl)
                .userId(userId)
                .build();
    }



}
