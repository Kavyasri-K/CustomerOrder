package com.customer.orderservice.service;

import com.customer.orderservice.dto.CreateOrderRequest;
import com.customer.orderservice.dto.OrderResponse;
import com.customer.orderservice.dto.UpdateOrderStatusRequest;
import com.customer.orderservice.exception.InvalidOrderStatusException;
import com.customer.orderservice.exception.OrderNotFoundException;
import com.customer.orderservice.model.Order;
import com.customer.orderservice.model.OrderStatus;
import com.customer.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = Order.builder()
                .id(1L)
                .customerId("12345")
                .product("Laptop")
                .quantity(1)
                .price(1200.0)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully create an order with status CREATED")
    void testCreateOrder() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("12345")
                .product("Laptop")
                .quantity(1)
                .price(1200.0)
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        OrderResponse response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCustomerId()).isEqualTo("12345");
        assertThat(response.getProduct()).isEqualTo("Laptop");
        assertThat(response.getQuantity()).isEqualTo(1);
        assertThat(response.getPrice()).isEqualTo(1200.0);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should retrieve order by ID when order exists")
    void testGetOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        OrderResponse response = orderService.getOrderById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCustomerId()).isEqualTo("12345");
        assertThat(response.getProduct()).isEqualTo("Laptop");

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not exist")
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id: 999");

        verify(orderRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should retrieve all orders")
    void testGetAllOrders() {
        Order secondOrder = Order.builder()
                .id(2L)
                .customerId("67890")
                .product("Smartphone")
                .quantity(2)
                .price(800.0)
                .status(OrderStatus.SHIPPED)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findAll()).thenReturn(Arrays.asList(sampleOrder, secondOrder));

        List<OrderResponse> orders = orderService.getAllOrders();

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getId()).isEqualTo(1L);
        assertThat(orders.get(1).getId()).isEqualTo(2L);

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should successfully update order status")
    void testUpdateOrderStatus_Success() {
        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                .status("SHIPPED")
                .build();

        Order updatedOrder = Order.builder()
                .id(1L)
                .customerId("12345")
                .product("Laptop")
                .quantity(1)
                .price(1200.0)
                .status(OrderStatus.SHIPPED)
                .createdAt(sampleOrder.getCreatedAt())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        OrderResponse response = orderService.updateOrderStatus(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw InvalidOrderStatusException when an invalid status is provided")
    void testUpdateOrderStatus_InvalidStatus() {
        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                .status("INVALID_STATUS")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, request))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("Invalid status: 'INVALID_STATUS'");

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when updating status for non-existent order")
    void testUpdateOrderStatus_NotFound() {
        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                .status("SHIPPED")
                .build();

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, request))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id: 999");

        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }
}
