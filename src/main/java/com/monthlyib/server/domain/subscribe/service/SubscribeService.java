package com.monthlyib.server.domain.subscribe.service;


import com.monthlyib.server.api.subscribe.dto.SubscribePostDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeResponseDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeUserPatchDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeUserResponseDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.SubscribeStatus;
import com.monthlyib.server.domain.subscribe.entity.Subscribe;
import com.monthlyib.server.domain.subscribe.entity.SubscribeUser;
import com.monthlyib.server.domain.subscribe.repository.SubscribeRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.service.UserService;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;

    private final UserService userService;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "subscribePlans", key = "'all'")
    public List<SubscribeResponseDto> findAllSubscribe() {
        return subscribeRepository.findAllSubscribes()
                .stream().map(SubscribeResponseDto::of).toList();
    }

    public SubscribeUserResponseDto createSubscribeUser(Long subscribeId, User user, Long userId) {
        verifyAdmin(user);
        Subscribe subscribe = verifySubscribe(subscribeId);
        User findUser = userService.findUserEntity(userId);
        SubscribeUser newSubUser = SubscribeUser.create(subscribe, findUser);
        SubscribeUser saveSubUser = subscribeRepository.saveSubscribeUser(newSubUser);
        return SubscribeUserResponseDto.of(saveSubUser);
    }

    public SubscribeUserResponseDto updateSubscribeUser(Long subscribeUserId, Long newSubscribeId, SubscribeUserPatchDto dto, User user) {
        verifyAdmin(user);
        SubscribeUser findSubUser = verifySubUser(subscribeUserId);
        if (newSubscribeId != null && !newSubscribeId.equals(findSubUser.getSubscribeId())) {
            Subscribe nextSubscribe = verifySubscribe(newSubscribeId);
            findSubUser.applySubscribe(nextSubscribe);
        }
        SubscribeUser update = findSubUser.update(dto);
        SubscribeUser saveSubUser = subscribeRepository.saveSubscribeUser(update);
        return SubscribeUserResponseDto.of(saveSubUser);
    }

    @Transactional(readOnly = true)
    public Page<SubscribeUserResponseDto> findAllSubscribeUser(Long userId, int page, User user) {
        verifyAdminOrSelf(user, userId);
        return subscribeRepository.findAllSubscribeByUserId(userId, PageRequest.of(page, 10, Sort.by("createAt").descending()))
                .map(SubscribeUserResponseDto::of);
    }

    @Transactional(readOnly = true)
    public SubscribeUserResponseDto findActiveSubscribeUser(Long userId, User user) {
        verifyAdminOrSelf(user, userId);
        return subscribeRepository.findSubscribeUserByUserIdAndStatus(userId, SubscribeStatus.ACTIVE)
                .map(SubscribeUserResponseDto::of)
                .orElse(null);
    }

    public SubscribeUser requireActiveSubscribe(User user) {
        if (user.getAuthority() == Authority.ADMIN) {
            return null;
        }
        return verifyActiveSubUser(user.getUserId());
    }

    public Long consumeQuestionAccess(User user) {
        SubscribeUser activeSubscribe = requireActiveSubscribe(user);
        if (activeSubscribe == null) {
            return null;
        }
        if (!activeSubscribe.canAskQuestion()) {
            throw new ServiceLogicException(ErrorCode.SUBSCRIBE_QUESTION_LIMIT_EXCEEDED);
        }
        activeSubscribe.consumeQuestion();
        subscribeRepository.saveSubscribeUser(activeSubscribe);
        return activeSubscribe.getSubscribeUserId();
    }

    public void restoreQuestionAccess(Long subscribeUserId) {
        restoreConsumedCount(subscribeUserId, RestoreTarget.QUESTION);
    }

    public Long consumeTutoringAccess(User user) {
        SubscribeUser activeSubscribe = requireActiveSubscribe(user);
        if (activeSubscribe == null) {
            return null;
        }
        if (!activeSubscribe.canCreateTutoring()) {
            throw new ServiceLogicException(ErrorCode.SUBSCRIBE_TUTORING_LIMIT_EXCEEDED);
        }
        activeSubscribe.consumeTutoring();
        subscribeRepository.saveSubscribeUser(activeSubscribe);
        return activeSubscribe.getSubscribeUserId();
    }

    public void restoreTutoringAccess(Long subscribeUserId) {
        restoreConsumedCount(subscribeUserId, RestoreTarget.TUTORING);
    }

    public void ensureCourseAccessible(User user, Long videoLessonsId) {
        if (user.getAuthority() == Authority.ADMIN) {
            return;
        }
        SubscribeUser activeSubscribe = verifyActiveSubUser(user.getUserId());
        if (!activeSubscribe.hasCourseAccess(videoLessonsId)) {
            throw new ServiceLogicException(ErrorCode.SUBSCRIBE_VIDEO_LESSONS_ACCESS_DENIED);
        }
    }

    public void consumeCourseAccess(User user, Long videoLessonsId) {
        if (user.getAuthority() == Authority.ADMIN) {
            return;
        }

        SubscribeUser activeSubscribe = verifyActiveSubUser(user.getUserId());
        if (activeSubscribe.hasCourseAccess(videoLessonsId)) {
            return;
        }
        if (!activeSubscribe.canConsumeCourse()) {
            throw new ServiceLogicException(ErrorCode.SUBSCRIBE_VIDEO_LESSONS_LIMIT_EXCEEDED);
        }
        activeSubscribe.grantCourseAccess(videoLessonsId);
        subscribeRepository.saveSubscribeUser(activeSubscribe);
    }


    @CacheEvict(cacheNames = "subscribePlans", allEntries = true)
    public SubscribeResponseDto createSubscribe(SubscribePostDto dto, User user) {
        verifyAdmin(user);
        Subscribe newSub = Subscribe.create(dto);
        return SubscribeResponseDto.of(subscribeRepository.save(newSub));
    }

    @CacheEvict(cacheNames = "subscribePlans", allEntries = true)
    public SubscribeResponseDto updateSubscribe(Long subscribeId, SubscribePostDto dto, User user) {
        verifyAdmin(user);
        Subscribe find = verifySubscribe(subscribeId);
        Subscribe updateSub = find.update(dto);
        return SubscribeResponseDto.of(subscribeRepository.save(updateSub));

    }

    @CacheEvict(cacheNames = "subscribePlans", allEntries = true)
    public void deleteSubscribe(Long subscribeId, User user) {
        verifyAdmin(user);
        subscribeRepository.deleteById(subscribeId);
    }

    private Subscribe verifySubscribe(Long subscribeId) {
        return subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    }

    private SubscribeUser verifyActiveSubUser(Long userId) {
        return subscribeRepository.findSubscribeUserByUserIdAndStatus(userId, SubscribeStatus.ACTIVE)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_ACTIVE_SUBSCRIBE));
    }

    private SubscribeUser verifySubUser(Long subscribeUserId) {
        return subscribeRepository.findSubUser(subscribeUserId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    }

    private void verifyAdmin(User user) {
        if (!user.getAuthority().equals(Authority.ADMIN)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void verifyAdminOrSelf(User user, Long userId) {
        if (!user.getAuthority().equals(Authority.ADMIN) && !user.getUserId().equals(userId)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void restoreConsumedCount(Long subscribeUserId, RestoreTarget target) {
        if (subscribeUserId == null) {
            return;
        }

        SubscribeUser subscribeUser = subscribeRepository.findSubUser(subscribeUserId).orElse(null);
        if (subscribeUser == null) {
            return;
        }

        switch (target) {
            case QUESTION -> subscribeUser.restoreQuestion();
            case TUTORING -> subscribeUser.restoreTutoring();
        }
        subscribeRepository.saveSubscribeUser(subscribeUser);
    }

    private enum RestoreTarget {
        QUESTION,
        TUTORING
    }



}
