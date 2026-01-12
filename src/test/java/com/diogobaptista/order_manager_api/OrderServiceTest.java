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
import java.util.Collections;
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

        service = new OrderService(orderRepository, stockRepository, allocator, itemRepository, userRepository, fileLogService);
    }

    @Test
    void create_success() {
        Item item = new Item();
        item.setId(1L);
        User user = new User();
        user.setId(2L);

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setItemId(1L);
        dto.setUserId(2L);
        dto.setQuantity(5);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Order savedOrder = new Order();
        savedOrder.setItem(item);
        savedOrder.setUser(user);
        savedOrder.setQuantity(5);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(stockRepository.findAll()).thenReturn(Collections.emptyList());

        Order result = service.create(dto);

        assertNotNull(result);
        assertEquals(item, result.getItem());
        assertEquals(user, result.getUser());
        assertEquals(5, result.getQuantity());

        verify(orderRepository).save(any(Order.class));
        verify(allocator, never()).fulfillOrderWithStockMovement(any(Order.class), any(StockMovement.class));
    }

    @Test
    void create_itemNotFound_throwsException() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setItemId(1L);
        dto.setUserId(2L);

        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> service.create(dto));
        assertTrue(exception.getMessage().contains("Item not found with id=1"));
        verify(orderRepository, never()).save(any(Order.class));
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

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> service.create(dto));
        assertTrue(exception.getMessage().contains("User not found with id=2"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void findAll_returnsOrders() {
        Order o1 = new Order();
        Order o2 = new Order();

        when(orderRepository.findAll()).thenReturn(Arrays.asList(o1, o2));

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
