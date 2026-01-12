package com.diogobaptista.order_manager_api.controller;

import com.diogobaptista.order_manager_api.dto.ItemRequestDTO;
import com.diogobaptista.order_manager_api.dto.ItemResponseDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import com.diogobaptista.order_manager_api.mapper.ItemMapper;
import com.diogobaptista.order_manager_api.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService service;
    private final ItemMapper mapper;

    public ItemController(ItemService service, ItemMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> getAll() {
        List<ItemResponseDTO> items = service.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ItemResponseDTO> create(@Valid @RequestBody ItemRequestDTO dto) {
        Item saved = service.createItem(dto);
        return ResponseEntity.status(201).body(mapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> update(@PathVariable Long id,@Valid @RequestBody ItemRequestDTO dto) {
        return service.updateItem(id, dto)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = service.deleteItem(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
