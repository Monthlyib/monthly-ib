package com.monthlyib.server.domain.order.service;


import com.monthlyib.server.api.subscribe.dto.SubscribeUserResponseDto;
import com.monthlyib.server.domain.order.dto.OrderDto;
import com.monthlyib.server.domain.order.dto.OrderResponseDto;
import com.monthlyib.server.domain.order.entity.IbOrder;
import com.monthlyib.server.domain.order.repository.IbOrderJpaRepository;
import com.monthlyib.server.domain.subscribe.service.SubscribeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IbOrderService {

    private final SubscribeService subscribeService;

    private final IbOrderJpaRepository ibOrderJpaRepository;

    public OrderResponseDto createIbOrder(OrderDto orderDto, Long userId, Long subscribeId) {
        IbOrder ibOrder = IbOrder.create(orderDto, userId, subscribeId);
        IbOrder saveOrder = ibOrderJpaRepository.save(ibOrder);
        SubscribeUserResponseDto subscribeUser = subscribeService.createSubscribeUser(subscribeId, null, userId);
        return OrderResponseDto.of(saveOrder);
    }


    public Page<OrderResponseDto> findOrderByUserId(Long userId, int page, int size) {
        return ibOrderJpaRepository.findAllByUserId(userId, PageRequest.of(page, size, Sort.by("createAt").descending()))
                .map(OrderResponseDto::of);
    }

    public Page<OrderResponseDto> findOrderBySubscribeId(Long subscribeId, int page, int size) {
        return ibOrderJpaRepository.findAllBySubscribeId(subscribeId, PageRequest.of(page, size, Sort.by("createAt").descending()))
                .map(OrderResponseDto::of);
    }
}
