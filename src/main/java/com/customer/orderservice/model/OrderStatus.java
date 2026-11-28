package com.customer.orderservice.model;

public enum OrderStatus {
    CREATED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /**
     * Allowed lifecycle:
     * CREATED -> PROCESSING -> SHIPPED -> DELIVERED
     * CREATED / PROCESSING -> CANCELLED
     * DELIVERED and CANCELLED are terminal.
     */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case CREATED -> next == PROCESSING || next == CANCELLED;
            case PROCESSING -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
