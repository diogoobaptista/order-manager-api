package com.diogobaptista.order_manager_api;

import com.diogobaptista.order_manager_api.controller.OrderController;
import com.diogobaptista.order_manager_api.dto.OrderRequestDTO;
import com.diogobaptista.order_manager_api.dto.OrderResponseDTO;
import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.mapper.OrderMapper;
import com.diogobaptista.order_manager_api.service.OrderService;
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

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService service;

    @MockBean
    private OrderMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAll_ShouldReturnList() throws Exception {
        Order order = new Order();
        OrderResponseDTO responseDTO = new OrderResponseDTO(); // Assuming default constructor

        when(service.findAll()).thenReturn(Collections.singletonList(order));
        when(mapper.toDto(any(Order.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getById_ShouldReturnOrder() throws Exception {
        Long orderId = 1L;
        Order order = new Order();
        OrderResponseDTO responseDTO = new OrderResponseDTO();

        when(service.findById(orderId)).thenReturn(Optional.of(order));
        when(mapper.toDto(order)).thenReturn(responseDTO);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk());
    }

    @Test
    public void getById_ShouldReturnNotFound() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void create_ShouldReturnCreated() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO();
        Order savedOrder = new Order();
        OrderResponseDTO responseDTO = new OrderResponseDTO();

        when(service.create(any(OrderRequestDTO.class))).thenReturn(savedOrder);
        when(mapper.toDto(savedOrder)).thenReturn(responseDTO);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }
}