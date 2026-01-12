package com.diogobaptista.order_manager_api.controller;

import com.diogobaptista.order_manager_api.dto.OrderRequestDTO;
import com.diogobaptista.order_manager_api.dto.OrderResponseDTO;
import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.mapper.OrderMapper;
import com.diogobaptista.order_manager_api.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;
    private final OrderMapper mapper;

    public OrderController(OrderService service, OrderMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAll() {
        List<OrderResponseDTO> orders = service.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@RequestBody OrderRequestDTO dto) {
        Order saved = service.create(dto);
        return ResponseEntity.status(201).body(mapper.toDto(saved));
    }
}
