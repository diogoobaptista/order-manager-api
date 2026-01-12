package com.diogobaptista.order_manager_api.dto;

import javax.validation.constraints.NotNull;

public class ItemRequestDTO {

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Stock is required")
    private Integer stockQuantity;

    public ItemRequestDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
