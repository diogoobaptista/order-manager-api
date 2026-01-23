package com.diogobaptista.order_manager_api.dto;

public class StockMovementRequestDTO {

    private Long orderId;
    private int quantity;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
