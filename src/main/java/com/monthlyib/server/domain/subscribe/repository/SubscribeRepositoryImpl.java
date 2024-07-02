package com.monthlyib.server.domain.subscribe.repository;

import com.monthlyib.server.constant.SubscribeStatus;
import com.monthlyib.server.domain.subscribe.entity.Subscribe;
import com.monthlyib.server.domain.subscribe.entity.SubscribeUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubscribeRepositoryImpl implements SubscribeRepository{


    private final SubscribeJpaRepository subscribeJpaRepository;

    private final SubscribeUserJpaRepository subscribeUserJpaRepository;

    @Override
    public List<Subscribe> findAllSubscribes() {
        return subscribeJpaRepository.findAll();
    }

    @Override
    public List<Subscribe> findAllIdList(List<Long> subscribeIds) {
        return subscribeJpaRepository.findAllById(subscribeIds);
    }

    @Override
    public Page<SubscribeUser> findAllSubscribeByUserId(Long userId, Pageable pageable) {
        return subscribeUserJpaRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public Optional<SubscribeUser> findSubscribeUserByUserIdAndStatus(Long userId, SubscribeStatus subscribeStatus) {
        return subscribeUserJpaRepository.findByUserIdAndSubscribeStatus(userId, subscribeStatus);
    }

    @Override
    public Optional<SubscribeUser> findSubUser(Long subscribeUserId) {
        return subscribeUserJpaRepository.findById(subscribeUserId);
    }

    @Override
    public Optional<Subscribe> findById(Long subscriptionId) {
        return subscribeJpaRepository.findById(subscriptionId);
    }

    @Override
    public Subscribe save(Subscribe subscribe) {
        return subscribeJpaRepository.save(subscribe);
    }

    @Override
    public SubscribeUser saveSubscribeUser(SubscribeUser subscribeUser) {
        return subscribeUserJpaRepository.save(subscribeUser);
    }

    @Override
    public void deleteById(Long subscriptionId) {
        subscribeJpaRepository.deleteById(subscriptionId);
    }
}
