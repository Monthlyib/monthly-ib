package com.monthlyib.server.domain.order.repository;

import com.monthlyib.server.domain.order.entity.IbOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IbOrderJpaRepository extends JpaRepository<IbOrder, Long> {

    Optional<IbOrder> findByOrderId(String orderId);

    Page<IbOrder> findAllByUserId(Long userId, Pageable pageable);

    Page<IbOrder> findAllBySubscribeId(Long subscribeId, Pageable pageable);

}
