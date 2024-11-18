package com.monthlyib.server.api.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {

    private Long subscribeId;

    private String orderId;

    private String amount;

    private String paymentKey;

}
