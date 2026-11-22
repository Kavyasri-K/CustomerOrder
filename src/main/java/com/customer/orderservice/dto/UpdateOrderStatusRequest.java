package com.customer.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateOrderStatusRequest {

    @NotBlank(message = "Status is required and cannot be blank")
    private String status;

    public UpdateOrderStatusRequest() {
    }

    public UpdateOrderStatusRequest(String status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class Builder {

        private String status;

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public UpdateOrderStatusRequest build() {
            return new UpdateOrderStatusRequest(status);
        }
    }
}
