package com.customer.orderservice.validation;

import com.customer.orderservice.dto.CreateOrderRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Reject request when customerId is missing or empty")
    void shouldFailWhenCustomerIdIsMissing() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("")
                .product("Monitor")
                .quantity(2)
                .price(150.0)
                .build();

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("customerId"));
    }

    @Test
    @DisplayName("Reject request when quantity is zero or negative")
    void shouldFailWhenQuantityIsZeroOrNegative() {
        CreateOrderRequest requestWithZero = CreateOrderRequest.builder()
                .customerId("CUST-101")
                .product("Monitor")
                .quantity(0)
                .price(150.0)
                .build();

        Set<ConstraintViolation<CreateOrderRequest>> zeroViolations = validator.validate(requestWithZero);
        assertThat(zeroViolations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));

        CreateOrderRequest requestWithNegative = CreateOrderRequest.builder()
                .customerId("CUST-101")
                .product("Monitor")
                .quantity(-3)
                .price(150.0)
                .build();

        Set<ConstraintViolation<CreateOrderRequest>> negViolations = validator.validate(requestWithNegative);
        assertThat(negViolations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
    }

    @Test
    @DisplayName("Reject request when price is zero or negative")
    void shouldFailWhenPriceIsZeroOrNegative() {
        CreateOrderRequest requestWithZeroPrice = CreateOrderRequest.builder()
                .customerId("CUST-101")
                .product("Monitor")
                .quantity(1)
                .price(0.0)
                .build();

        Set<ConstraintViolation<CreateOrderRequest>> zeroPriceViolations = validator.validate(requestWithZeroPrice);
        assertThat(zeroPriceViolations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));

        CreateOrderRequest requestWithNegPrice = CreateOrderRequest.builder()
                .customerId("CUST-101")
                .product("Monitor")
                .quantity(1)
                .price(-10.50)
                .build();

        Set<ConstraintViolation<CreateOrderRequest>> negPriceViolations = validator.validate(requestWithNegPrice);
        assertThat(negPriceViolations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }

    @Test
    @DisplayName("Accept valid create order request")
    void shouldPassWhenAllFieldsValid() {
        CreateOrderRequest validRequest = CreateOrderRequest.builder()
                .customerId("CUST-101")
                .product("Monitor")
                .quantity(1)
                .price(299.99)
                .build();

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(validRequest);
        assertThat(violations).isEmpty();
    }
}
