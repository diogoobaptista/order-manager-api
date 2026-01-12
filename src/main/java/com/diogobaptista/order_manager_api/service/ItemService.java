package com.diogobaptista.order_manager_api.service;

import com.diogobaptista.order_manager_api.dto.ItemRequestDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import com.diogobaptista.order_manager_api.mapper.ItemMapper;
import com.diogobaptista.order_manager_api.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository repository;
    private final FileLogService fileLogService;
    private final ItemMapper mapper;

    public ItemService(ItemRepository repository, ItemMapper mapper, FileLogService fileLogService) {
        this.repository = repository;
        this.mapper = mapper;
        this.fileLogService = fileLogService;
    }

    public List<Item> findAll() {
        return repository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return repository.findById(id);
    }

    public Item createItem(ItemRequestDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            logAndWrite("Attempted to create item with empty name", "WARN");
            throw new IllegalArgumentException("Item name cannot be blank");
        }

        Item savedItem = repository.save(mapper.toEntity(dto));
        logAndWrite(String.format("Created Item [ID: %d, Name: %s]", savedItem.getId(), savedItem.getName()), "INFO");
        return savedItem;
    }

    public Optional<Item> updateItem(Long id, ItemRequestDTO dto) {
        return repository.findById(id)
                .map(existing -> {
                    mapper.updateEntity(existing, dto);
                    Item updated = repository.save(existing);
                    logAndWrite(String.format("Updated Item [ID: %d]", id), "INFO");
                    return updated;
                });
    }

    public boolean deleteItem(Long id) {
        return repository.findById(id)
                .map(item -> {
                    repository.delete(item);
                    logAndWrite(String.format("Deleted Item [ID: %d]", id), "INFO");
                    return true;
                })
                .orElseGet(() -> {
                    logAndWrite(String.format("Fail deleting Item [ID: %d] not found", id), "WARN");
                    return false;
                });
    }

    private void logAndWrite(String message, String level) {
        if ("WARN".equals(level)) {
            logger.warn(message);
        } else {
            logger.info(message);
        }
        fileLogService.appendLine(message);
    }
}