package com.diogobaptista.order_manager_api.dto;

public class StockMovementRequestDTO {
    public Long itemId;
    public int quantity;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

