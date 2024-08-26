package com.monthlyib.server.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monthlyib.server.domain.order.entity.IbOrder;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDto {

    private Long ibOrderId;
    private Long userId;
    private Long subscribeId;
    private String mId;
    private String version;
    private String paymentKey;
    private String status;
    private String lastTransactionKey;
    private String method;
    private String orderId;
    private String orderName;
    private String requestedAt;
    private String approvedAt;
    private boolean useEscrow;
    private boolean cultureExpense;
    private String type;
    private String country;
    private Receipt receipt;
    private Checkout checkout;
    private String currency;
    private int totalAmount;
    private int balanceAmount;
    private int suppliedAmount;
    private int vat;
    private int taxFreeAmount;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    

    @Data
    @AllArgsConstructor
    public static class Receipt {
        private String url;
    }

    @Data
    @AllArgsConstructor
    public static class Checkout {
        private String url;
    }

    public static OrderResponseDto of(IbOrder ibOrder) {
        return OrderResponseDto.builder()
                .ibOrderId(ibOrder.getIbOrderId())
                .userId(ibOrder.getUserId())
                .subscribeId(ibOrder.getSubscribeId())
                .mId(ibOrder.getMId())
                .version(ibOrder.getVersion())
                .paymentKey(ibOrder.getPaymentKey())
                .status(ibOrder.getStatus())
                .lastTransactionKey(ibOrder.getLastTransactionKey())
                .method(ibOrder.getMethod())
                .orderId(ibOrder.getOrderId())
                .orderName(ibOrder.getOrderName())
                .requestedAt(ibOrder.getRequestedAt())
                .approvedAt(ibOrder.getApprovedAt())
                .useEscrow(ibOrder.isUseEscrow())
                .cultureExpense(ibOrder.isCultureExpense())
                .type(ibOrder.getType())
                .country(ibOrder.getCountry())
                .receipt(new Receipt(ibOrder.getReceiptUrl()))
                .checkout(new Checkout(ibOrder.getCheckoutUrl()))
                .currency(ibOrder.getCurrency())
                .totalAmount(ibOrder.getTotalAmount())
                .balanceAmount(ibOrder.getBalanceAmount())
                .suppliedAmount(ibOrder.getSuppliedAmount())
                .vat(ibOrder.getVat())
                .taxFreeAmount(ibOrder.getTaxFreeAmount())
                .createAt(ibOrder.getCreateAt())
                .updateAt(ibOrder.getUpdateAt())
                .build();
    }
}