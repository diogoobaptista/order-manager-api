package com.diogobaptista.order_manager_api;

import com.diogobaptista.order_manager_api.dto.OrderRequestDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.entity.StockMovement;
import com.diogobaptista.order_manager_api.entity.User;
import com.diogobaptista.order_manager_api.repository.ItemRepository;
import com.diogobaptista.order_manager_api.repository.OrderRepository;
import com.diogobaptista.order_manager_api.repository.StockMovementRepository;
import com.diogobaptista.order_manager_api.repository.UserRepository;
import com.diogobaptista.order_manager_api.service.FileLogService;
import com.diogobaptista.order_manager_api.service.OrderAllocationService;
import com.diogobaptista.order_manager_api.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    private OrderRepository orderRepository;
    private StockMovementRepository stockRepository;
    private OrderAllocationService allocator;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private OrderService service;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        stockRepository = mock(StockMovementRepository.class);
        allocator = mock(OrderAllocationService.class);
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        FileLogService fileLogService = mock(FileLogService.class);

        service = new OrderService(
                orderRepository,
                stockRepository,
                allocator,
                itemRepository,
                userRepository,
                fileLogService
        );
    }

    @Test
    void create_success_noStockAvailable() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Mouse");
        item.setStockQuantity(0);

        User user = new User();
        user.setId(2L);
        user.setEmail("user@test.com");

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setItemId(1L);
        dto.setUserId(2L);
        dto.setQuantity(5);

        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setItem(item);
        savedOrder.setUser(user);
        savedOrder.setQuantity(5);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = service.create(dto);

        assertNotNull(result);
        assertEquals(item, result.getItem());
        assertEquals(user, result.getUser());
        assertEquals(5, result.getQuantity());

        verify(stockRepository, never()).save(any());
        verify(allocator, never())
                .fulfillOrderWithStockMovement(any(), any());
    }

    @Test
    void create_success_fullAllocation() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Keyboard");
        item.setStockQuantity(10);

        User user = new User();
        user.setId(2L);
        user.setEmail("user@test.com");

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setItemId(1L);
        dto.setUserId(2L);
        dto.setQuantity(5);

        Order savedOrder = new Order();
        savedOrder.setId(200L);
        savedOrder.setItem(item);
        savedOrder.setUser(user);
        savedOrder.setQuantity(5);

        StockMovement savedMovement = new StockMovement();
        savedMovement.setId(300L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(stockRepository.save(any(StockMovement.class))).thenReturn(savedMovement);

        Order result = service.create(dto);

        assertEquals(5, item.getStockQuantity());

        verify(stockRepository).save(any(StockMovement.class));
        verify(itemRepository).save(item);
        verify(allocator).fulfillOrderWithStockMovement(savedOrder, savedMovement);
    }

    @Test
    void create_success_partialAllocation() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Monitor");
        item.setStockQuantity(3);

        User user = new User();
        user.setId(2L);
        user.setEmail("user@test.com");

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setItemId(1L);
        dto.setUserId(2L);
        dto.setQuantity(5);

        Order savedOrder = new Order();
        savedOrder.setId(201L);
        savedOrder.setItem(item);
        savedOrder.setUser(user);
        savedOrder.setQuantity(5);

        StockMovement savedMovement = new StockMovement();
        savedMovement.setId(301L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(stockRepository.save(any(StockMovement.class))).thenReturn(savedMovement);

        Order result = service.create(dto);

        assertEquals(0, item.getStockQuantity());

        verify(stockRepository).save(any(StockMovement.class));
        verify(itemRepository).save(item);
        verify(allocator).fulfillOrderWithStockMovement(savedOrder, savedMovement);
    }

    @Test
    void create_itemNotFound_throwsException() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setItemId(1L);
        dto.setUserId(2L);

        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.create(dto));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_userNotFound_throwsException() {
        Item item = new Item();
        item.setId(1L);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setItemId(1L);
        dto.setUserId(2L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.create(dto));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void findAll_returnsOrders() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(new Order(), new Order()));

        List<Order> orders = service.findAll();

        assertEquals(2, orders.size());
    }

    @Test
    void findById_existingOrder_returnsOrder() {
        Order order = new Order();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<Order> result = service.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(order, result.get());
    }

    @Test
    void findById_nonExistingOrder_returnsEmpty() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Order> result = service.findById(1L);

        assertFalse(result.isPresent());
    }
}
