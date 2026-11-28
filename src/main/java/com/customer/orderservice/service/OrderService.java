package com.customer.orderservice.service;

import com.customer.orderservice.dto.CreateOrderRequest;
import com.customer.orderservice.dto.OrderResponse;
import com.customer.orderservice.dto.UpdateOrderStatusRequest;
import com.customer.orderservice.exception.InvalidOrderStatusException;
import com.customer.orderservice.exception.InvalidStatusTransitionException;
import com.customer.orderservice.exception.OrderNotFoundException;
import com.customer.orderservice.model.Order;
import com.customer.orderservice.model.OrderStatus;
import com.customer.orderservice.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.customerId())
                .product(request.product())
                .quantity(request.quantity())
                // normalise to 2 decimal places so the response matches what the DB stores
                .price(request.price().setScale(2, RoundingMode.HALF_UP))
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderResponse.fromEntity(order);
    }

    /**
     * Lists orders page by page. When customerId is null/blank, all orders are returned.
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(String customerId, Pageable pageable) {
        Page<Order> page = (customerId == null || customerId.isBlank())
                ? orderRepository.findAll(pageable)
                : orderRepository.findByCustomerId(customerId, pageable);
        return page.map(OrderResponse::fromEntity);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.status().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidOrderStatusException("Invalid status: '" + request.status()
                    + "'. Allowed values: " + Arrays.toString(OrderStatus.values()));
        }

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(order.getStatus(), newStatus);
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        return OrderResponse.fromEntity(updated);
    }
}
