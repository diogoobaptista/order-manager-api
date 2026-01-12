package com.diogobaptista.order_manager_api.dto;

import java.time.LocalDateTime;

public class StockMovementResponseDTO {

    private Long id;
    private Long itemId;
    private int quantity;
    private LocalDateTime creationDate;

    public StockMovementResponseDTO() {}

    public StockMovementResponseDTO(Long id, Long itemId, int quantity, LocalDateTime creationDate) {
        this.id = id;
        this.itemId = itemId;
        this.quantity = quantity;
        this.creationDate = creationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
