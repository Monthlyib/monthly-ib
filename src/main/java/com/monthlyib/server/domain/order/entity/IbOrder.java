package com.monthlyib.server.domain.order.entity;


import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.order.dto.OrderDto;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "ib_orders")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbOrder extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ibOrderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long subscribeId;

    @Column(nullable = false)
    private String mId;
    
    @Column(nullable = false)
    private String version;
    
    @Column(nullable = false)
    private String paymentKey;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private String lastTransactionKey;
    
    @Column(nullable = false)
    private String method;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String orderName;

    @Column(nullable = false)
    private String requestedAt;

    @Column(nullable = false)
    private String approvedAt;

    @Column(nullable = false)
    private boolean useEscrow;

    @Column(nullable = false)
    private boolean cultureExpense;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String  receiptUrl;

    @Column(nullable = false)
    private String  checkoutUrl;

    @Column(nullable = false)
    private String currency;
    
    @Column(nullable = false)
    private int totalAmount;

    @Column(nullable = false)
    private int balanceAmount;

    @Column(nullable = false)
    private int suppliedAmount;
    
    @Column(nullable = false)
    private int vat;

    @Column(nullable = false)
    private int taxFreeAmount;
    
    public static IbOrder create(OrderDto orderDto, Long userId, Long subscribeId) {
        String id = orderDto.getMId();
        if (id == null) {
            id = "";
        }
        return IbOrder.builder()
                .userId(userId)
                .subscribeId(subscribeId)
                .mId(id)
                .version(orderDto.getVersion())
                .paymentKey(orderDto.getPaymentKey())
                .status(orderDto.getStatus())
                .lastTransactionKey(orderDto.getLastTransactionKey())
                .method(orderDto.getMethod())
                .orderId(orderDto.getOrderId())
                .orderName(orderDto.getOrderName())
                .requestedAt(orderDto.getRequestedAt())
                .approvedAt(orderDto.getApprovedAt())
                .useEscrow(orderDto.isUseEscrow())
                .cultureExpense(orderDto.isCultureExpense())
                .type(orderDto.getType())
                .country(orderDto.getCountry())
                .receiptUrl(orderDto.getReceipt().getUrl())
                .checkoutUrl(orderDto.getCheckout().getUrl())
                .currency(orderDto.getCurrency())
                .totalAmount(orderDto.getTotalAmount())
                .balanceAmount(orderDto.getBalanceAmount())
                .suppliedAmount(orderDto.getSuppliedAmount())
                .vat(orderDto.getVat())
                .taxFreeAmount(orderDto.getTaxFreeAmount())
                .build();
    }
}
