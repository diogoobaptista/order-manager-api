package com.diogobaptista.order_manager_api.controller;

import com.diogobaptista.order_manager_api.dto.StockMovementRequestDTO;
import com.diogobaptista.order_manager_api.dto.StockMovementResponseDTO;
import com.diogobaptista.order_manager_api.mapper.StockMovementMapper;
import com.diogobaptista.order_manager_api.service.StockMovementService;
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
@RequestMapping("/stock-movements")
public class StockMovementController {

    private final StockMovementService service;
    private final StockMovementMapper mapper;

    public StockMovementController(StockMovementService service,
                                   StockMovementMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<StockMovementResponseDTO>> getAll() {
        return ResponseEntity.ok(
                service.findAll()
                        .stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockMovementResponseDTO> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StockMovementResponseDTO> create(@RequestBody StockMovementRequestDTO dto) {
        return service.createStockMovement(dto)
                .map(mapper::toDto)
                .map(stockMovementResponseDTO -> ResponseEntity.status(201).body(stockMovementResponseDTO))
                .orElse(ResponseEntity.badRequest().build());
    }
}
