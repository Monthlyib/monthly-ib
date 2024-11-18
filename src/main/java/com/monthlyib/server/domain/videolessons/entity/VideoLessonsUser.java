package com.monthlyib.server.domain.videolessons.entity;

import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.VideoLessonsStatus;
import com.monthlyib.server.constant.VideoLessonsUserStatus;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "video_lessons_user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsUser extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoLessonsUserId;

    @Column(nullable = false)
    private Long videoLessonsId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VideoLessonsUserStatus status;

}
