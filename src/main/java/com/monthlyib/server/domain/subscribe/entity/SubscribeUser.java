package com.monthlyib.server.domain.subscribe.entity;


import com.monthlyib.server.api.subscribe.dto.SubscribeUserPatchDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.SubscribeStatus;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@Entity
@Table(
        name = "subscribe_user",
        indexes = {
                @Index(name = "idx_subscribe_user_user_status_created", columnList = "user_id,subscribe_status,create_at")
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeUser extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscribeUserId;

    @Column(nullable = false)
    private Long subscribeId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userNickName;

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

    // 만료 기한
    @Column(nullable = false)
    private LocalDate expirationDate;

    // 구독 일자
    @Column(nullable = false)
    private LocalDate subscriptionDate;

    // 구독 날짜
    @Column(nullable = false)
    private int subscriptionDay;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Long> videoLessonsIdList;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscribeStatus subscribeStatus;

    public static SubscribeUser create(Subscribe subscribe, User user) {
        return SubscribeUser.builder()
                .userId(user.getUserId())
                .subscribeId(subscribe.getSubscribeId())
                .userName(user.getUsername())
                .userNickName(user.getNickName())
                .title(subscribe.getTitle())
                .content(subscribe.getContent())
                .price(subscribe.getPrice())
                .questionCount(subscribe.getQuestionCount())
                .unlimitedQuestions(subscribe.isUnlimitedQuestions())
                .tutoringCount(subscribe.getTutoringCount())
                .unlimitedTutoring(subscribe.isUnlimitedTutoring())
                .subscribeMonthPeriod(subscribe.getSubscribeMonthPeriod())
                .videoLessonsCount(subscribe.getVideoLessonsCount())
                .unlimitedVideoLessons(subscribe.isUnlimitedVideoLessons())
                .expirationDate(LocalDate.now().plusMonths(subscribe.getSubscribeMonthPeriod()))
                .videoLessonsIdList(new ArrayList<>())
                .subscriptionDate(LocalDate.now())
                .subscriptionDay(LocalDate.now().getDayOfMonth())
                .subscribeStatus(SubscribeStatus.WAIT)
                .build();
    }

    public SubscribeUser update(SubscribeUserPatchDto dto) {
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
        this.subscribeStatus = Optional.ofNullable(dto.getSubscribeStatus()).orElse(this.subscribeStatus);
        this.expirationDate = LocalDate.now().plusMonths(this.subscribeMonthPeriod);
        return this;
    }

    public SubscribeUser applySubscribe(Subscribe subscribe) {
        this.subscribeId = subscribe.getSubscribeId();
        this.title = subscribe.getTitle();
        this.content = subscribe.getContent();
        this.price = subscribe.getPrice();
        this.questionCount = subscribe.getQuestionCount();
        this.unlimitedQuestions = subscribe.isUnlimitedQuestions();
        this.tutoringCount = subscribe.getTutoringCount();
        this.unlimitedTutoring = subscribe.isUnlimitedTutoring();
        this.subscribeMonthPeriod = subscribe.getSubscribeMonthPeriod();
        this.videoLessonsCount = subscribe.getVideoLessonsCount();
        this.unlimitedVideoLessons = subscribe.isUnlimitedVideoLessons();
        this.videoLessonsIdList = new ArrayList<>();
        this.expirationDate = LocalDate.now().plusMonths(subscribe.getSubscribeMonthPeriod());
        return this;
    }

    public boolean canAskQuestion() {
        return unlimitedQuestions || questionCount > 0;
    }

    public void consumeQuestion() {
        if (!unlimitedQuestions) {
            this.questionCount = Math.max(0, this.questionCount - 1);
        }
    }

    public void restoreQuestion() {
        if (!unlimitedQuestions) {
            this.questionCount += 1;
        }
    }

    public boolean canCreateTutoring() {
        return unlimitedTutoring || tutoringCount > 0;
    }

    public void consumeTutoring() {
        if (!unlimitedTutoring) {
            this.tutoringCount = Math.max(0, this.tutoringCount - 1);
        }
    }

    public void restoreTutoring() {
        if (!unlimitedTutoring) {
            this.tutoringCount += 1;
        }
    }

    public boolean hasCourseAccess(Long videoLessonsId) {
        return unlimitedVideoLessons
                || Optional.ofNullable(videoLessonsIdList).orElseGet(ArrayList::new).contains(videoLessonsId);
    }

    public boolean canConsumeCourse() {
        return unlimitedVideoLessons || videoLessonsCount > 0;
    }

    public boolean grantCourseAccess(Long videoLessonsId) {
        if (unlimitedVideoLessons) {
            return false;
        }
        if (videoLessonsIdList == null) {
            videoLessonsIdList = new ArrayList<>();
        }
        if (videoLessonsIdList.contains(videoLessonsId)) {
            return false;
        }
        this.videoLessonsIdList.add(videoLessonsId);
        this.videoLessonsCount = Math.max(0, this.videoLessonsCount - 1);
        return true;
    }

}
