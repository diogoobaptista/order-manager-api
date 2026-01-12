package com.diogobaptista.order_manager_api.mapper;


import com.diogobaptista.order_manager_api.dto.UserRequestDTO;
import com.diogobaptista.order_manager_api.dto.UserResponseDTO;
import com.diogobaptista.order_manager_api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponse(User user) {
        if (user == null) return null;
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }

    public User toEntity(UserRequestDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return user;
    }

    public void updateEntity(User user, UserRequestDTO dto) {
        if (user != null && dto != null) {
            user.setName(dto.getName());
            user.setEmail(dto.getEmail());
        }
    }
}