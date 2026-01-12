package com.diogobaptista.order_manager_api.dto;

public class ItemResponseDTO {

    private Long id;
    private String name;
    private Integer stockQuantity;

    public ItemResponseDTO(Long id, String name, Integer stockQuantity) {
        this.id = id;
        this.name = name;
        this.stockQuantity = stockQuantity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setId(long id) {
        this.id = id;
    }
}
