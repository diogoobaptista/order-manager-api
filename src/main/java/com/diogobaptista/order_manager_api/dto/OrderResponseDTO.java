package com.diogobaptista.order_manager_api.dto;

import java.time.LocalDateTime;

public class OrderResponseDTO {
    private Long id;
    private int quantity;
    private int fulfilledQuantity;
    private boolean complete;
    private Long itemId;
    private Long userId;
    private LocalDateTime creationDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getFulfilledQuantity() { return fulfilledQuantity; }
    public void setFulfilledQuantity(int fulfilledQuantity) { this.fulfilledQuantity = fulfilledQuantity; }

    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
}
