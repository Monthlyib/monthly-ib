package com.monthlyib.server.domain.subscribe.entity;

import com.monthlyib.server.api.subscribe.dto.SubscribePostDto;
import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "subscribe")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscribe extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscribeId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int questionCount;

    @Column(nullable = false)
    private int tutoringCount;

    @Column(nullable = false)
    private int subscribeMonthPeriod;

    @Column(nullable = false)
    private int videoLessonsCount;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String fontColor;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Long> videoLessonsIdList;

    public static Subscribe create(SubscribePostDto dto) {
        return Subscribe.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .price(dto.getPrice())
                .questionCount(dto.getQuestionCount())
                .tutoringCount(dto.getTutoringCount())
                .subscribeMonthPeriod(dto.getSubscribeMonthPeriod())
                .videoLessonsCount(dto.getVideoLessonsCount())
                .videoLessonsIdList(dto.getVideoLessonsIdList())
                .color(dto.getColor())
                .fontColor(dto.getFontColor())
                .build();
    }

    public Subscribe update(SubscribePostDto dto) {
        this.title = Optional.ofNullable(dto.getTitle()).orElse(this.title);
        this.content = Optional.ofNullable(dto.getContent()).orElse(this.content);
        this.price = Optional.ofNullable(dto.getPrice()).orElse(this.price);
        this.questionCount = Optional.ofNullable(dto.getQuestionCount()).orElse(this.questionCount);
        this.tutoringCount = Optional.ofNullable(dto.getTutoringCount()).orElse(this.tutoringCount);
        this.subscribeMonthPeriod = Optional.ofNullable(dto.getSubscribeMonthPeriod()).orElse(this.subscribeMonthPeriod);
        this.videoLessonsCount = Optional.ofNullable(dto.getVideoLessonsCount()).orElse(this.videoLessonsCount);
        this.videoLessonsIdList = Optional.ofNullable(dto.getVideoLessonsIdList()).orElse(this.videoLessonsIdList);
        this.color = Optional.ofNullable(dto.getColor()).orElse(this.color);
        this.fontColor = Optional.ofNullable(dto.getFontColor()).orElse(this.fontColor);
        return this;
    }


}
