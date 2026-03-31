package com.monthlyib.server.domain.tutoring.entity;

import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "tutoring_email_templates")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutoringEmailTemplate extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String bodyTemplate;

    @Column(nullable = false)
    private boolean active;

    public static TutoringEmailTemplate createDefault() {
        return TutoringEmailTemplate.builder()
                .subject("튜터링 신청이 승인되었습니다.")
                .bodyTemplate("{nickName}님, 신청하신 튜터링이 승인되었습니다.\n예약 일시: {date} {time}")
                .active(true)
                .build();
    }
}
