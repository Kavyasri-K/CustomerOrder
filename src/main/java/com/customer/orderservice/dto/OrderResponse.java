package com.customer.orderservice.dto;

import com.customer.orderservice.model.Order;
import com.customer.orderservice.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String customerId,
        String product,
        Integer quantity,
        BigDecimal price,
        OrderStatus status,
        LocalDateTime createdAt
) {

    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getProduct(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                order.getCreatedAt());
    }
}
