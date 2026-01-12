package com.diogobaptista.order_manager_api.mapper;

import com.diogobaptista.order_manager_api.dto.StockMovementRequestDTO;
import com.diogobaptista.order_manager_api.dto.StockMovementResponseDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import com.diogobaptista.order_manager_api.entity.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovement toEntity(StockMovementRequestDTO dto, Item item) {
        StockMovement sm = new StockMovement();
        sm.setItem(item);
        sm.setQuantity(dto.getQuantity());
        return sm;
    }

    public StockMovementResponseDTO toDto(StockMovement entity) {
        return new StockMovementResponseDTO(
                entity.getId(),
                entity.getItem().getId(),
                entity.getQuantity(),
                entity.getCreationDate()
        );
    }
}
