package com.diogobaptista.order_manager_api.mapper;

import com.diogobaptista.order_manager_api.dto.OrderResponseDTO;
import com.diogobaptista.order_manager_api.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponseDTO toDto(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setQuantity(order.getQuantity());
        dto.setFulfilledQuantity(order.getFulfilledQuantity());
        dto.setComplete(order.isComplete());
        dto.setItemId(order.getItem() != null ? order.getItem().getId() : null);
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        dto.setCreationDate(order.getCreationDate());
        return dto;
    }
}
