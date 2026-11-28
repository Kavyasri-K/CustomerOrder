package com.customer.orderservice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.customer.orderservice.model.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    @DisplayName("Allowed forward transitions")
    void allowedTransitions() {
        assertThat(CREATED.canTransitionTo(PROCESSING)).isTrue();
        assertThat(CREATED.canTransitionTo(CANCELLED)).isTrue();
        assertThat(PROCESSING.canTransitionTo(SHIPPED)).isTrue();
        assertThat(PROCESSING.canTransitionTo(CANCELLED)).isTrue();
        assertThat(SHIPPED.canTransitionTo(DELIVERED)).isTrue();
    }

    @Test
    @DisplayName("Backward, skipping and same-state transitions are rejected")
    void rejectedTransitions() {
        assertThat(CREATED.canTransitionTo(SHIPPED)).isFalse();
        assertThat(CREATED.canTransitionTo(CREATED)).isFalse();
        assertThat(SHIPPED.canTransitionTo(PROCESSING)).isFalse();
        assertThat(SHIPPED.canTransitionTo(CANCELLED)).isFalse();
    }

    @Test
    @DisplayName("DELIVERED and CANCELLED are terminal")
    void terminalStates() {
        for (OrderStatus next : OrderStatus.values()) {
            assertThat(DELIVERED.canTransitionTo(next)).isFalse();
            assertThat(CANCELLED.canTransitionTo(next)).isFalse();
        }
    }
}
