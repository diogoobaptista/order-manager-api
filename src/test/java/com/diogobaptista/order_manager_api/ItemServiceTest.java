package com.diogobaptista.order_manager_api;

import com.diogobaptista.order_manager_api.dto.ItemRequestDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import com.diogobaptista.order_manager_api.mapper.ItemMapper;
import com.diogobaptista.order_manager_api.repository.ItemRepository;
import com.diogobaptista.order_manager_api.service.FileLogService;
import com.diogobaptista.order_manager_api.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ItemServiceTest {

    private ItemRepository repository;
    private ItemMapper mapper;
    private ItemService service;

    @BeforeEach
    public void setup() {
        repository = mock(ItemRepository.class);
        mapper = mock(ItemMapper.class);
        FileLogService fileLogService = mock(FileLogService.class);
        service = new ItemService(repository, mapper, fileLogService);
    }

    @Test
    public void createItem_success() {
        ItemRequestDTO dto = new ItemRequestDTO();
        dto.setName("Item A");

        Item entity = new Item();
        entity.setName("Item A");

        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        Item result = service.createItem(dto);

        assertNotNull(result);
        assertEquals("Item A", result.getName());
        verify(repository).save(entity);
    }

    @Test
    public void findById_existingItem_returnsItem() {
        Item item = new Item();
        item.setName("Item 1");

        when(repository.findById(1L)).thenReturn(Optional.of(item));

        Optional<Item> result = service.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("Item 1", result.get().getName());
    }

    @Test
    public void findById_nonExistingItem_returnsEmpty() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<Item> result = service.findById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    public void updateItem_existingItem_success() {
        ItemRequestDTO dto = new ItemRequestDTO();
        dto.setName("Updated Item");

        Item existing = new Item();
        existing.setName("Old Item");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            existing.setName(dto.getName());
            return null;
        }).when(mapper).updateEntity(existing, dto);

        when(repository.save(existing)).thenReturn(existing);

        Optional<Item> updated = service.updateItem(1L, dto);
        assertTrue(updated.isPresent());
        assertEquals("Updated Item", updated.get().getName());
    }

    @Test
    public void deleteItem_existingItem_returnsTrue() {
        Item item = new Item();
        item.setName("Item 1");

        when(repository.findById(1L)).thenReturn(Optional.of(item));

        boolean deleted = service.deleteItem(1L);
        assertTrue(deleted);
        verify(repository).delete(item);
    }

}