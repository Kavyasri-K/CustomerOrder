package com.customer.orderservice.validation;

import com.customer.orderservice.dto.CreateOrderRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
        CreateOrderRequest request = new CreateOrderRequest("", "Monitor", 2, new BigDecimal("150.00"));

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("customerId"));
    }

    @Test
    @DisplayName("Reject request when quantity is zero or negative")
    void shouldFailWhenQuantityIsZeroOrNegative() {
        CreateOrderRequest requestWithZero =
                new CreateOrderRequest("CUST-101", "Monitor", 0, new BigDecimal("150.00"));

        Set<ConstraintViolation<CreateOrderRequest>> zeroViolations = validator.validate(requestWithZero);
        assertThat(zeroViolations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));

        CreateOrderRequest requestWithNegative =
                new CreateOrderRequest("CUST-101", "Monitor", -3, new BigDecimal("150.00"));

        Set<ConstraintViolation<CreateOrderRequest>> negViolations = validator.validate(requestWithNegative);
        assertThat(negViolations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
    }

    @Test
    @DisplayName("Reject request when price is zero or negative")
    void shouldFailWhenPriceIsZeroOrNegative() {
        CreateOrderRequest requestWithZeroPrice =
                new CreateOrderRequest("CUST-101", "Monitor", 1, BigDecimal.ZERO);

        Set<ConstraintViolation<CreateOrderRequest>> zeroPriceViolations = validator.validate(requestWithZeroPrice);
        assertThat(zeroPriceViolations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));

        CreateOrderRequest requestWithNegPrice =
                new CreateOrderRequest("CUST-101", "Monitor", 1, new BigDecimal("-10.50"));

        Set<ConstraintViolation<CreateOrderRequest>> negPriceViolations = validator.validate(requestWithNegPrice);
        assertThat(negPriceViolations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }

    @Test
    @DisplayName("Accept valid create order request")
    void shouldPassWhenAllFieldsValid() {
        CreateOrderRequest validRequest =
                new CreateOrderRequest("CUST-101", "Monitor", 1, new BigDecimal("299.99"));

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(validRequest);
        assertThat(violations).isEmpty();
    }
}
