package com.monthlyib.server.domain.subscribe.entity;

import com.monthlyib.server.api.subscribe.dto.SubscribePostDto;
import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private boolean unlimitedQuestions;

    @Column(nullable = false)
    private int tutoringCount;

    @Column(nullable = false)
    private boolean unlimitedTutoring;

    @Column(nullable = false)
    private int subscribeMonthPeriod;

    @Column(nullable = false)
    private int videoLessonsCount;

    @Column(nullable = false)
    private boolean unlimitedVideoLessons;

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
                .questionCount(Optional.ofNullable(dto.getQuestionCount()).orElse(0))
                .unlimitedQuestions(Optional.ofNullable(dto.getUnlimitedQuestions()).orElse(false))
                .tutoringCount(Optional.ofNullable(dto.getTutoringCount()).orElse(0))
                .unlimitedTutoring(Optional.ofNullable(dto.getUnlimitedTutoring()).orElse(false))
                .subscribeMonthPeriod(Optional.ofNullable(dto.getSubscribeMonthPeriod()).orElse(0))
                .videoLessonsCount(Optional.ofNullable(dto.getVideoLessonsCount()).orElse(0))
                .unlimitedVideoLessons(Optional.ofNullable(dto.getUnlimitedVideoLessons()).orElse(false))
                .videoLessonsIdList(new ArrayList<>(Optional.ofNullable(dto.getVideoLessonsIdList()).orElseGet(ArrayList::new)))
                .color(dto.getColor())
                .fontColor(dto.getFontColor())
                .build();
    }

    public Subscribe update(SubscribePostDto dto) {
        this.title = Optional.ofNullable(dto.getTitle()).orElse(this.title);
        this.content = Optional.ofNullable(dto.getContent()).orElse(this.content);
        this.price = Optional.ofNullable(dto.getPrice()).orElse(this.price);
        this.questionCount = Optional.ofNullable(dto.getQuestionCount()).orElse(this.questionCount);
        this.unlimitedQuestions = Optional.ofNullable(dto.getUnlimitedQuestions()).orElse(this.unlimitedQuestions);
        this.tutoringCount = Optional.ofNullable(dto.getTutoringCount()).orElse(this.tutoringCount);
        this.unlimitedTutoring = Optional.ofNullable(dto.getUnlimitedTutoring()).orElse(this.unlimitedTutoring);
        this.subscribeMonthPeriod = Optional.ofNullable(dto.getSubscribeMonthPeriod()).orElse(this.subscribeMonthPeriod);
        this.videoLessonsCount = Optional.ofNullable(dto.getVideoLessonsCount()).orElse(this.videoLessonsCount);
        this.unlimitedVideoLessons = Optional.ofNullable(dto.getUnlimitedVideoLessons()).orElse(this.unlimitedVideoLessons);
        this.videoLessonsIdList = Optional.ofNullable(dto.getVideoLessonsIdList())
                .map(list -> new ArrayList<>(list))
                .orElseGet(() -> this.videoLessonsIdList == null ? new ArrayList<>() : new ArrayList<>(this.videoLessonsIdList));
        this.color = Optional.ofNullable(dto.getColor()).orElse(this.color);
        this.fontColor = Optional.ofNullable(dto.getFontColor()).orElse(this.fontColor);
        return this;
    }


}
