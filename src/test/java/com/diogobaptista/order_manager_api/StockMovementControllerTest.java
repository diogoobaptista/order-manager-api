package com.diogobaptista.order_manager_api;

import com.diogobaptista.order_manager_api.controller.StockMovementController;
import com.diogobaptista.order_manager_api.dto.StockMovementRequestDTO;
import com.diogobaptista.order_manager_api.dto.StockMovementResponseDTO;
import com.diogobaptista.order_manager_api.entity.StockMovement;
import com.diogobaptista.order_manager_api.mapper.StockMovementMapper;
import com.diogobaptista.order_manager_api.service.StockMovementService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockMovementController.class)
public class StockMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockMovementService service;

    @MockBean
    private StockMovementMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAll_ShouldReturnList() throws Exception {
        StockMovement movement = new StockMovement();
        StockMovementResponseDTO responseDTO = new StockMovementResponseDTO();

        when(service.findAll()).thenReturn(Collections.singletonList(movement));
        when(mapper.toDto(any(StockMovement.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/stock-movements"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getById_Success() throws Exception {
        Long id = 1L;
        StockMovement movement = new StockMovement();
        StockMovementResponseDTO responseDTO = new StockMovementResponseDTO();

        when(service.findById(id)).thenReturn(Optional.of(movement));
        when(mapper.toDto(movement)).thenReturn(responseDTO);

        mockMvc.perform(get("/stock-movements/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    public void getById_NotFound() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/stock-movements/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void create_Success() throws Exception {
        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO();
        StockMovement movement = new StockMovement();
        StockMovementResponseDTO responseDTO = new StockMovementResponseDTO();

        when(service.createStockMovement(any(StockMovementRequestDTO.class)))
                .thenReturn(Optional.of(movement));
        when(mapper.toDto(movement)).thenReturn(responseDTO);

        mockMvc.perform(post("/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    public void create_Failure() throws Exception {
        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO();

        when(service.createStockMovement(any(StockMovementRequestDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }
}