package com.monthlyib.server.domain.subscribe.repository;

import com.monthlyib.server.constant.SubscribeStatus;
import com.monthlyib.server.domain.subscribe.entity.Subscribe;
import com.monthlyib.server.domain.subscribe.entity.SubscribeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SubscribeRepository {

    List<Subscribe> findAllSubscribes();

    List<Subscribe> findAllIdList(List<Long> subscribeIds);

    Page<SubscribeUser> findAllSubscribeByUserId(Long userId, Pageable pageable);

    Optional<SubscribeUser> findSubscribeUserByUserIdAndStatus(Long userId, SubscribeStatus subscribeStatus);

    Optional<SubscribeUser> findSubUser(Long subscribeUserId);

    Optional<Subscribe> findById(Long subscriptionId);

    Subscribe save(Subscribe subscribe);

    SubscribeUser saveSubscribeUser(SubscribeUser subscribeUser);

    void deleteById(Long subscriptionId);
}
