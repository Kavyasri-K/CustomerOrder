package com.customer.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(
        @NotBlank(message = "Status is required and cannot be blank")
        String status
) {
}
