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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
                .price(new BigDecimal("1200.00"))
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully create an order with status CREATED")
    void testCreateOrder() {
        CreateOrderRequest request = new CreateOrderRequest("12345", "Laptop", 1, new BigDecimal("1200"));

        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        OrderResponse response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.customerId()).isEqualTo("12345");
        assertThat(response.product()).isEqualTo("Laptop");
        assertThat(response.quantity()).isEqualTo(1);
        assertThat(response.price()).isEqualByComparingTo("1200.00");
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should save price rounded to 2 decimal places")
    void testCreateOrder_PriceScale() {
        CreateOrderRequest request = new CreateOrderRequest("12345", "Laptop", 1, new BigDecimal("19.999"));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.price()).isEqualTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("Should retrieve order by ID when order exists")
    void testGetOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        OrderResponse response = orderService.getOrderById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.customerId()).isEqualTo("12345");
        assertThat(response.product()).isEqualTo("Laptop");

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
    @DisplayName("Should retrieve a page of all orders when no customerId is given")
    void testGetOrders_All() {
        Order secondOrder = Order.builder()
                .id(2L)
                .customerId("67890")
                .product("Smartphone")
                .quantity(2)
                .price(new BigDecimal("800.00"))
                .status(OrderStatus.SHIPPED)
                .createdAt(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(sampleOrder, secondOrder), pageable, 2));

        Page<OrderResponse> page = orderService.getOrders(null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).id()).isEqualTo(1L);
        assertThat(page.getContent().get(1).id()).isEqualTo(2L);

        verify(orderRepository, times(1)).findAll(pageable);
        verify(orderRepository, never()).findByCustomerId(any(), any());
    }

    @Test
    @DisplayName("Should filter by customerId when provided")
    void testGetOrders_ByCustomer() {
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findByCustomerId("12345", pageable))
                .thenReturn(new PageImpl<>(List.of(sampleOrder), pageable, 1));

        Page<OrderResponse> page = orderService.getOrders("12345", pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).customerId()).isEqualTo("12345");

        verify(orderRepository, times(1)).findByCustomerId("12345", pageable);
        verify(orderRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should treat a blank customerId as 'no filter'")
    void testGetOrders_BlankCustomerId() {
        Pageable pageable = PageRequest.of(0, 20);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(sampleOrder), pageable, 1));

        Page<OrderResponse> page = orderService.getOrders("  ", pageable);

        assertThat(page.getContent()).hasSize(1);
        verify(orderRepository, never()).findByCustomerId(any(), any());
    }

    @Test
    @DisplayName("Should successfully update order status with a valid transition")
    void testUpdateOrderStatus_Success() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PROCESSING");

        Order updatedOrder = Order.builder()
                .id(1L)
                .customerId("12345")
                .product("Laptop")
                .quantity(1)
                .price(new BigDecimal("1200.00"))
                .status(OrderStatus.PROCESSING)
                .createdAt(sampleOrder.getCreatedAt())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        OrderResponse response = orderService.updateOrderStatus(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.PROCESSING);

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should reject a status transition that skips a step (CREATED -> DELIVERED)")
    void testUpdateOrderStatus_InvalidTransition() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("DELIVERED");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, request))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Cannot change order status from CREATED to DELIVERED");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should reject any change once an order is DELIVERED")
    void testUpdateOrderStatus_FromTerminalState() {
        sampleOrder.setStatus(OrderStatus.DELIVERED);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("CREATED");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, request))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw InvalidOrderStatusException when an invalid status is provided")
    void testUpdateOrderStatus_InvalidStatus() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("INVALID_STATUS");

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
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PROCESSING");

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, request))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id: 999");

        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }
}
