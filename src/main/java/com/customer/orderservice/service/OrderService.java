package com.customer.orderservice.service;

import com.customer.orderservice.dto.CreateOrderRequest;
import com.customer.orderservice.dto.OrderResponse;
import com.customer.orderservice.dto.UpdateOrderStatusRequest;
import com.customer.orderservice.exception.InvalidOrderStatusException;
import com.customer.orderservice.exception.OrderNotFoundException;
import com.customer.orderservice.model.Order;
import com.customer.orderservice.model.OrderStatus;
import com.customer.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .product(request.getProduct())
                .quantity(request.getQuantity())
                .price(request.getPrice())
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

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidOrderStatusException("Invalid status: '" + request.getStatus()
                    + "'. Allowed values: " + Arrays.toString(OrderStatus.values()));
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        return OrderResponse.fromEntity(updated);
    }
}
