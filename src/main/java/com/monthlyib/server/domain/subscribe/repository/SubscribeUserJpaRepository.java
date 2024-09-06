package com.monthlyib.server.domain.subscribe.repository;

import com.monthlyib.server.constant.SubscribeStatus;
import com.monthlyib.server.domain.subscribe.entity.SubscribeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscribeUserJpaRepository extends JpaRepository<SubscribeUser, Long> {

    Page<SubscribeUser> findAllByUserId(Long userId, Pageable pageable);

    Optional<SubscribeUser> findByUserIdAndSubscribeStatus(Long userId, SubscribeStatus subscribeStatus);

    List<SubscribeUser> findByExpirationDateBeforeAndSubscribeStatus(LocalDate expirationDate, SubscribeStatus subscribeStatus);
}
