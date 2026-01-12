package com.diogobaptista.order_manager_api;

import com.diogobaptista.order_manager_api.controller.UserController;
import com.diogobaptista.order_manager_api.dto.UserRequestDTO;
import com.diogobaptista.order_manager_api.dto.UserResponseDTO;
import com.diogobaptista.order_manager_api.entity.User;
import com.diogobaptista.order_manager_api.mapper.UserMapper;
import com.diogobaptista.order_manager_api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;

    @MockBean
    private UserMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAll_ShouldReturnList() throws Exception {
        User user = new User();
        UserResponseDTO responseDTO = new UserResponseDTO(1L, "John Doe", "john@example.com");

        when(service.findAll()).thenReturn(Collections.singletonList(user));
        when(mapper.toResponse(any(User.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    public void getById_Success() throws Exception {
        Long id = 1L;
        User user = new User();
        UserResponseDTO responseDTO = new UserResponseDTO(id, "John Doe", "john@example.com");

        when(service.findById(id)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(responseDTO);

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void getById_NotFound() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void create_Success() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setName("Jane Doe");
        requestDTO.setEmail("jane@example.com");

        User savedUser = new User();
        UserResponseDTO responseDTO = new UserResponseDTO(1L, "Jane Doe", "jane@example.com");

        when(service.createUser(any(UserRequestDTO.class))).thenReturn(savedUser);
        when(mapper.toResponse(savedUser)).thenReturn(responseDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    public void update_Success() throws Exception {
        Long id = 1L;
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setName("John Updated");

        User updatedUser = new User();
        UserResponseDTO responseDTO = new UserResponseDTO(id, "John Updated", "john@example.com");

        when(service.updateUser(eq(id), any(UserRequestDTO.class))).thenReturn(Optional.of(updatedUser));
        when(mapper.toResponse(updatedUser)).thenReturn(responseDTO);

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));
    }

    @Test
    public void delete_Success() throws Exception {
        when(service.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_NotFound() throws Exception {
        when(service.deleteUser(99L)).thenReturn(false);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound());
    }
}