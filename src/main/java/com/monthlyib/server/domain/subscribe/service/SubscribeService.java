package com.monthlyib.server.domain.subscribe.service;


import com.monthlyib.server.api.subscribe.dto.SubscribePostDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeResponseDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeUserPatchDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeUserResponseDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.SubscribeStatus;
import com.monthlyib.server.domain.subscribe.entity.Subscribe;
import com.monthlyib.server.domain.subscribe.entity.SubscribeUser;
import com.monthlyib.server.domain.subscribe.repository.SubscribeRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.service.UserService;
import com.monthlyib.server.exception.ServiceLogicException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;

    private final UserService userService;

    public List<SubscribeResponseDto> findAllSubscribe() {
        return subscribeRepository.findAllSubscribes()
                .stream().map(SubscribeResponseDto::of).toList();
    }

    public SubscribeUserResponseDto createSubscribeUser(Long subscribeId, User user, Long userId) {
        verifyActiveSubUserThrowError(userId);
        Subscribe subscribe = verifySubscribe(subscribeId);
        User findUser = userService.findUserEntity(userId);
        SubscribeUser newSubUser = SubscribeUser.create(subscribe, findUser);
        SubscribeUser saveSubUser = subscribeRepository.saveSubscribeUser(newSubUser);
        return SubscribeUserResponseDto.of(saveSubUser);
    }

    public SubscribeUserResponseDto createSubscribeUserByOrderConfirm(Long subscribeId, Long userId) {
        Subscribe subscribe = verifySubscribe(subscribeId);
        User findUser = userService.findUserEntity(userId);
        SubscribeUser newSubUser = SubscribeUser.create(subscribe, findUser, SubscribeStatus.ACTIVE);
        SubscribeUser saveSubUser = subscribeRepository.saveSubscribeUser(newSubUser);
        return SubscribeUserResponseDto.of(saveSubUser);
    }

    public SubscribeUserResponseDto updateSubscribeUser(Long subscribeUserId, SubscribeUserPatchDto dto, User user) {
        SubscribeUser findSubUser = verifySubUser(subscribeUserId);
        SubscribeUser update = findSubUser.update(dto);
        SubscribeUser saveSubUser = subscribeRepository.saveSubscribeUser(update);
        return SubscribeUserResponseDto.of(saveSubUser);
    }

    public Page<SubscribeUserResponseDto> findAllSubscribeUser(Long userId, int page, User user) {
        return subscribeRepository.findAllSubscribeByUserId(userId, PageRequest.of(page, 10, Sort.by("createAt").descending()))
                .map(SubscribeUserResponseDto::of);
    }


    public SubscribeResponseDto createSubscribe(SubscribePostDto dto, User user) {
        Subscribe newSub = Subscribe.create(dto);
        return SubscribeResponseDto.of(subscribeRepository.save(newSub));
    }

    public SubscribeResponseDto updateSubscribe(Long subscribeId, SubscribePostDto dto, User user) {
        Subscribe find = verifySubscribe(subscribeId);
        Subscribe updateSub = find.update(dto);
        return SubscribeResponseDto.of(subscribeRepository.save(updateSub));

    }

    public void deleteSubscribe(Long subscribeId) {
        subscribeRepository.deleteById(subscribeId);
    }

    private Subscribe verifySubscribe(Long subscribeId) {
        return subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    }

    public SubscribeUserResponseDto verifyActiveSubUser(Long userId) {
        Optional<SubscribeUser> find = subscribeRepository.findSubscribeUserByUserIdAndStatus(userId, SubscribeStatus.ACTIVE);
        return find.map(SubscribeUserResponseDto::of).orElse(null);
    }

    public void verifyActiveSubUserThrowError(Long userId) {
        Optional<SubscribeUser> find = subscribeRepository.findSubscribeUserByUserIdAndStatus(userId, SubscribeStatus.ACTIVE);
        if (find.isPresent()) {
            throw new ServiceLogicException(ErrorCode.ALREADY_ACTIVE_SUBSCRIBE);
        }
    }

    private SubscribeUser verifySubUser(Long subscribeUserId) {
        return subscribeRepository.findSubUser(subscribeUserId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    }



}
