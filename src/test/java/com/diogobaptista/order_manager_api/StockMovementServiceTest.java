package com.diogobaptista.order_manager_api;

import com.diogobaptista.order_manager_api.dto.StockMovementRequestDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.entity.StockMovement;
import com.diogobaptista.order_manager_api.mapper.StockMovementMapper;
import com.diogobaptista.order_manager_api.repository.ItemRepository;
import com.diogobaptista.order_manager_api.repository.OrderRepository;
import com.diogobaptista.order_manager_api.repository.StockMovementRepository;
import com.diogobaptista.order_manager_api.service.FileLogService;
import com.diogobaptista.order_manager_api.service.OrderAllocationService;
import com.diogobaptista.order_manager_api.service.StockMovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockMovementServiceTest {

    private StockMovementRepository stockRepo;
    private OrderRepository orderRepo;
    private ItemRepository itemRepo;
    private OrderAllocationService allocator;
    private StockMovementService service;
    private StockMovementMapper mapper;

    @BeforeEach
    public void setup() {
        stockRepo = mock(StockMovementRepository.class);
        itemRepo = mock(ItemRepository.class);
        orderRepo = mock(OrderRepository.class);
        allocator = mock(OrderAllocationService.class);
        mapper = mock(StockMovementMapper.class);
        FileLogService fileLogService = mock(FileLogService.class);

        service = new StockMovementService(stockRepo, itemRepo, orderRepo, allocator, fileLogService, mapper);
    }

    @Test
    public void create_shouldAllocateStockToIncompleteOrders() {
        Item item = new Item();
        item.setId(1L);
        item.setStockQuantity(10);

        StockMovementRequestDTO dto = new StockMovementRequestDTO();
        dto.setItemId(1L);
        dto.setQuantity(10);

        StockMovement sm = new StockMovement();
        sm.setId(100L);
        sm.setItem(item);
        sm.setQuantity(10);

        Order order = new Order();
        order.setId(50L);
        order.setItem(item);
        order.setQuantity(5);
        order.setFulfilledQuantity(0);

        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(dto, item)).thenReturn(sm);
        when(stockRepo.save(any())).thenReturn(sm);
        when(orderRepo.findAll()).thenReturn(Collections.singletonList(order));

        Optional<StockMovement> result = service.createStockMovement(dto);

        assertTrue(result.isPresent());
        verify(allocator, times(1)).fulfillOrderWithStockMovement(order, sm);
    }

    @Test
    public void create_shouldNotAllocateToCompletedOrders() {
        Item item = new Item();
        item.setId(2L);
        item.setStockQuantity(10);

        StockMovementRequestDTO dto = new StockMovementRequestDTO();
        dto.setItemId(2L);
        dto.setQuantity(10);

        StockMovement stock = new StockMovement();
        stock.setItem(item);
        stock.setQuantity(10);

        Order order = new Order();
        order.setItem(item);
        order.setQuantity(10);
        order.setFulfilledQuantity(10);

        when(itemRepo.findById(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(dto, item)).thenReturn(stock);
        when(stockRepo.save(any())).thenReturn(stock);
        when(orderRepo.findAll()).thenReturn(Collections.singletonList(order));

        Optional<StockMovement> result = service.createStockMovement(dto);

        assertFalse(result.isPresent());
        verify(allocator, never()).fulfillOrderWithStockMovement(any(), any());
    }

    @Test
    public void create_shouldReturnEmpty_whenItemNotFound() {
        when(itemRepo.findById(13L)).thenReturn(Optional.empty());

        StockMovementRequestDTO dto = new StockMovementRequestDTO();
        dto.setItemId(13L);
        dto.setQuantity(1);

        Optional<StockMovement> result = service.createStockMovement(dto);

        assertFalse(result.isPresent());
        verifyNoInteractions(allocator);
    }

    @Test
    public void create_shouldReturnEmpty_whenNoPendingOrdersExist() {
        Item item = new Item();
        item.setId(1L);
        item.setStockQuantity(10);

        StockMovementRequestDTO dto = new StockMovementRequestDTO();
        dto.setItemId(1L);
        dto.setQuantity(10);

        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepo.findAll()).thenReturn(Collections.emptyList());

        Optional<StockMovement> result = service.createStockMovement(dto);

        assertFalse(result.isPresent());
        verify(allocator, never()).fulfillOrderWithStockMovement(any(), any());
        verify(stockRepo, never()).save(any());
    }
}
