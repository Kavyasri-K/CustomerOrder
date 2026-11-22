package com.customer.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotBlank(message = "Customer ID is required and cannot be blank")
    private String customerId;

    @NotBlank(message = "Product is required and cannot be blank")
    private String product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String customerId, String product, Integer quantity, Double price) {
        this.customerId = customerId;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public static class Builder {
        private String customerId;
        private String product;
        private Integer quantity;
        private Double price;

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder product(String product) {
            this.product = product;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder price(Double price) {
            this.price = price;
            return this;
        }

        public CreateOrderRequest build() {
            return new CreateOrderRequest(customerId, product, quantity, price);
        }
    }
}
