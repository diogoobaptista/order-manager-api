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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository repository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderAllocationService orderAllocationService;
    @Mock
    private FileLogService fileLogService;
    @Mock
    private StockMovementMapper mapper;

    @InjectMocks
    private StockMovementService service;


    @Test
    void findAll_shouldReturnAllStockMovements() {
        List<StockMovement> list = Arrays.asList(
                new StockMovement(),
                new StockMovement()
        );
        when(repository.findAll()).thenReturn(list);

        List<StockMovement> result = service.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findById_shouldReturnOptional() {
        StockMovement sm = new StockMovement();
        when(repository.findById(1L)).thenReturn(Optional.of(sm));

        Optional<StockMovement> result = service.findById(1L);

        assertTrue(result.isPresent());
    }


    @Test
    void create_shouldCreateAndAllocateStockMovement() {
        Item item = new Item();
        item.setId(1L);
        item.setStockQuantity(10);

        Order order = new Order();
        order.setId(1L);
        order.setItem(item);
        order.setQuantity(5);
        order.setFulfilledQuantity(2);

        StockMovementRequestDTO dto = new StockMovementRequestDTO();
        dto.setOrderId(1L);
        dto.setQuantity(3);

        StockMovement sm = new StockMovement();
        sm.setId(99L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(mapper.toEntity(dto, item)).thenReturn(sm);
        when(repository.save(any())).thenReturn(sm);

        Optional<StockMovement> result = service.createStockMovement(dto);

        assertTrue(result.isPresent());
        assertEquals(7, item.getStockQuantity());

        verify(itemRepository).save(item);
        verify(orderAllocationService).fulfillOrderWithStockMovement(order, sm);
        verify(fileLogService).appendLine(contains("StockMovement"));
    }

    /* ---------- Exceptions ---------- */

    @Test
    void create_shouldThrow_whenOrderNotFound() {
        StockMovementRequestDTO dto = new StockMovementRequestDTO();
        dto.setOrderId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.createStockMovement(dto));
    }

    @Test
    void create_shouldThrow_whenOrderCompleted() {
        Order order = new Order();
        order.setId(1L);
        order.setFulfilledQuantity(5);
        order.setQuantity(5);

        StockMovementRequestDTO dto = new StockMovementRequestDTO();
        dto.setOrderId(1L);
        dto.setQuantity(1);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> service.createStockMovement(dto));

        verify(fileLogService).appendLine(contains("already completed"));
    }

    @Test
    void create_shouldThrow_whenRequestedQtyExceedsRemaining() {
        Item item = new Item();
        item.setStockQuantity(10);

        Order order = new Order();
        order.setId(1L);
        order.setItem(item);
        order.setQuantity(5);
        order.setFulfilledQuantity(4);

        StockMovementRequestDTO dto = new StockMovementRequestDTO();
        dto.setOrderId(1L);
        dto.setQuantity(5);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class,
                () -> service.createStockMovement(dto));

        verify(fileLogService).appendLine(contains("Invalid StockMovement"));
    }

    @Test
    void create_shouldThrow_whenNoStockAvailable() {
        Item item = new Item();
        item.setId(1L);
        item.setStockQuantity(0);

        Order order = new Order();
        order.setId(1L);
        order.setItem(item);
        order.setQuantity(5);
        order.setFulfilledQuantity(0);

        StockMovementRequestDTO dto = new StockMovementRequestDTO();
        dto.setOrderId(1L);
        dto.setQuantity(1);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> service.createStockMovement(dto));

        verify(fileLogService).appendLine(contains("No stock available"));
    }

    @Test
    void create_shouldAllocateMinBetweenRequestedAndAvailableStock() {
        Item item = new Item();
        item.setStockQuantity(2);

        Order order = new Order();
        order.setId(1L);
        order.setItem(item);
        order.setQuantity(10);
        order.setFulfilledQuantity(5);

        StockMovementRequestDTO dto = new StockMovementRequestDTO();
        dto.setOrderId(1L);
        dto.setQuantity(5);

        StockMovement sm = new StockMovement();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(mapper.toEntity(dto, item)).thenReturn(sm);
        when(repository.save(any())).thenReturn(sm);

        service.createStockMovement(dto);

        assertEquals(0, item.getStockQuantity());
        assertEquals(2, sm.getQuantity());
    }

}

