package com.customer.orderservice.integration;

import com.customer.orderservice.model.Order;
import com.customer.orderservice.model.OrderStatus;
import com.customer.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class OrderIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OrderRepository orderRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Test: Successfully create order")
    void testCreateOrder() throws Exception {
        String requestJson = """
            {
                "customerId": "12345",
                "product": "Laptop",
                "quantity": 1,
                "price": 1200
            }
            """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerId", is("12345")))
                .andExpect(jsonPath("$.product", is("Laptop")))
                .andExpect(jsonPath("$.quantity", is(1)))
                .andExpect(jsonPath("$.price", is(1200.0)))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    @DisplayName("Integration Test: Retrieve order by ID")
    void testRetrieveOrder() throws Exception {
        Order savedOrder = orderRepository.save(Order.builder()
                .customerId("12345")
                .product("Laptop")
                .quantity(1)
                .price(new BigDecimal("1200.00"))
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/orders/{id}", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedOrder.getId().intValue())))
                .andExpect(jsonPath("$.customerId", is("12345")))
                .andExpect(jsonPath("$.product", is("Laptop")))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    @DisplayName("Integration Test: Update order status")
    void testUpdateOrderStatus() throws Exception {
        Order savedOrder = orderRepository.save(Order.builder()
                .customerId("12345")
                .product("Laptop")
                .quantity(1)
                .price(new BigDecimal("1200.00"))
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build());

        String updateJson = """
            {
                "status": "PROCESSING"
            }
            """;

        mockMvc.perform(put("/orders/{id}/status", savedOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedOrder.getId().intValue())))
                .andExpect(jsonPath("$.status", is("PROCESSING")));
    }

    @Test
    @DisplayName("Integration Test: Retrieve all orders")
    void testRetrieveAllOrders() throws Exception {
        orderRepository.save(Order.builder()
                .customerId("12345")
                .product("Laptop")
                .quantity(1)
                .price(new BigDecimal("1200.00"))
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build());

        orderRepository.save(Order.builder()
                .customerId("67890")
                .product("Smartphone")
                .quantity(2)
                .price(new BigDecimal("800.00"))
                .status(OrderStatus.PROCESSING)
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page.totalElements", is(2)))
                // default sort is id DESC (newest first)
                .andExpect(jsonPath("$.content[0].customerId", is("67890")))
                .andExpect(jsonPath("$.content[1].customerId", is("12345")));
    }

    @Test
    @DisplayName("Integration Test: Validation failure on create order")
    void testCreateOrderValidationFailure() throws Exception {
        String invalidJson = """
            {
                "customerId": "",
                "product": "",
                "quantity": 0,
                "price": -10
            }
            """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("Integration Test: Retrieve non-existent order returns 404")
    void testRetrieveNonExistentOrder() throws Exception {
        mockMvc.perform(get("/orders/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Order not found with id: 9999")));
    }

    @Test
    @DisplayName("Integration Test: Update status of non-existent order returns 404")
    void testUpdateStatusNotFound() throws Exception {
        String updateJson = """
            {
                "status": "SHIPPED"
            }
            """;

        mockMvc.perform(put("/orders/{id}/status", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("Integration Test: Update status with invalid status returns 400")
    void testUpdateStatusInvalid() throws Exception {
        Order savedOrder = orderRepository.save(Order.builder()
                .customerId("12345")
                .product("Laptop")
                .quantity(1)
                .price(new BigDecimal("1200.00"))
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build());

        String invalidStatusJson = """
            {
                "status": "INVALID_STATE"
            }
            """;

        mockMvc.perform(put("/orders/{id}/status", savedOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidStatusJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid Status")))
                .andExpect(jsonPath("$.message", containsString("Invalid status: 'INVALID_STATE'")));
    }

    @Test
    @DisplayName("Integration Test: Filter orders by customerId")
    void testFilterByCustomerId() throws Exception {
        orderRepository.save(Order.builder()
                .customerId("12345").product("Laptop").quantity(1)
                .price(new BigDecimal("1200.00")).status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now()).build());
        orderRepository.save(Order.builder()
                .customerId("12345").product("Mouse").quantity(1)
                .price(new BigDecimal("20.00")).status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now()).build());
        orderRepository.save(Order.builder()
                .customerId("67890").product("Phone").quantity(1)
                .price(new BigDecimal("800.00")).status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now()).build());

        mockMvc.perform(get("/orders").param("customerId", "12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].customerId", everyItem(is("12345"))))
                .andExpect(jsonPath("$.page.totalElements", is(2)));
    }

    @Test
    @DisplayName("Integration Test: Pagination returns the requested page size")
    void testPagination() throws Exception {
        for (int i = 1; i <= 5; i++) {
            orderRepository.save(Order.builder()
                    .customerId("12345").product("Item " + i).quantity(1)
                    .price(new BigDecimal("10.00")).status(OrderStatus.CREATED)
                    .createdAt(LocalDateTime.now()).build());
        }

        mockMvc.perform(get("/orders").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page.size", is(2)))
                .andExpect(jsonPath("$.page.totalElements", is(5)))
                .andExpect(jsonPath("$.page.totalPages", is(3)));
    }

    @Test
    @DisplayName("Integration Test: Illegal status transition returns 409")
    void testUpdateStatusInvalidTransition() throws Exception {
        Order savedOrder = orderRepository.save(Order.builder()
                .customerId("12345")
                .product("Laptop")
                .quantity(1)
                .price(new BigDecimal("1200.00"))
                .status(OrderStatus.DELIVERED)
                .createdAt(LocalDateTime.now())
                .build());

        String json = """
            {
                "status": "CREATED"
            }
            """;

        mockMvc.perform(put("/orders/{id}/status", savedOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Invalid Status Transition")))
                .andExpect(jsonPath("$.message",
                        containsString("Cannot change order status from DELIVERED to CREATED")));
    }
}
