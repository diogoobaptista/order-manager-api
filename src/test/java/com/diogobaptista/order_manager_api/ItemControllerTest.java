package com.diogobaptista.order_manager_api;

import com.diogobaptista.order_manager_api.controller.ItemController;
import com.diogobaptista.order_manager_api.dto.ItemRequestDTO;
import com.diogobaptista.order_manager_api.dto.ItemResponseDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import com.diogobaptista.order_manager_api.mapper.ItemMapper;
import com.diogobaptista.order_manager_api.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService service;

    @MockBean
    private ItemMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createItem_Success() throws Exception {
        ItemRequestDTO request = new ItemRequestDTO();
        request.setName("Mechanical Keyboard");
        request.setStockQuantity(50);

        Item savedItem = new Item();
        ItemResponseDTO response = new ItemResponseDTO(1L, "Mechanical Keyboard", 50);

        when(service.createItem(any(ItemRequestDTO.class))).thenReturn(savedItem);
        when(mapper.toDto(savedItem)).thenReturn(response);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.stockQuantity").value(50));
    }

    @Test
    public void createItem_ValidationError() throws Exception {
        ItemRequestDTO invalidRequest = new ItemRequestDTO();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getById_Success() throws Exception {
        Long itemId = 1L;
        Item item = new Item();
        ItemResponseDTO response = new ItemResponseDTO(itemId, "Gaming Mouse", 100);

        when(service.findById(itemId)).thenReturn(Optional.of(item));
        when(mapper.toDto(item)).thenReturn(response);

        mockMvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Gaming Mouse"));
    }

    @Test
    public void delete_NotFound() throws Exception {
        when(service.deleteItem(99L)).thenReturn(false);

        mockMvc.perform(delete("/items/99"))
                .andExpect(status().isNotFound());
    }
}