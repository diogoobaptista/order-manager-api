package com.diogobaptista.order_manager_api.mapper;

import com.diogobaptista.order_manager_api.dto.ItemRequestDTO;
import com.diogobaptista.order_manager_api.dto.ItemResponseDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public Item toEntity(ItemRequestDTO dto) {
        Item item = new Item();
        item.setName(dto.getName());
        item.setStockQuantity(dto.getStockQuantity());
        return item;
    }

    public void updateEntity(Item item, ItemRequestDTO dto) {
        item.setName(dto.getName());
        item.setStockQuantity(dto.getStockQuantity());
    }

    public ItemResponseDTO toDto(Item item) {
        return new ItemResponseDTO(item.getId(), item.getName(), item.getStockQuantity());
    }
}
