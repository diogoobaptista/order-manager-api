package com.diogobaptista.order_manager_api;

import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.entity.StockMovement;
import com.diogobaptista.order_manager_api.entity.User;
import com.diogobaptista.order_manager_api.repository.OrderRepository;
import com.diogobaptista.order_manager_api.service.EmailService;
import com.diogobaptista.order_manager_api.service.FileLogService;
import com.diogobaptista.order_manager_api.service.OrderAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class OrderAllocationServiceTest {

    private OrderRepository orderRepository;
    private EmailService emailService;
    private OrderAllocationService orderAllocationService;

    @BeforeEach
    public void setup() {
        orderRepository = mock(OrderRepository.class);
        emailService = mock(EmailService.class);
        FileLogService fileLogService = mock(FileLogService.class);
        orderAllocationService = new OrderAllocationService(orderRepository, emailService, fileLogService);
    }

    @Test
    public void fulfill_shouldFulfillButNotCompleteOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setQuantity(10);
        order.setFulfilledQuantity(3);
        order.setUser(new User());

        StockMovement stock = new StockMovement();
        stock.setId(2L);
        stock.setQuantity(4);

        orderAllocationService.fulfillOrderWithStockMovement(order, stock);

        assertEquals(7, order.getFulfilledQuantity());
        verify(orderRepository).save(order);
        verify(emailService, never()).sendOrderCompleted(any(), any());
    }

    @Test
    public void fulfill_shouldDoNothing_whenNoStockAvailable() {
        Order order = new Order();
        order.setId(1L);
        order.setQuantity(10);
        order.setFulfilledQuantity(5);

        StockMovement stock = new StockMovement();
        stock.setId(2L);
        stock.setQuantity(0);

        orderAllocationService.fulfillOrderWithStockMovement(order, stock);

        verifyNoInteractions(orderRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    public void fulfill_shouldDoNothing_whenOrderAlreadyCompleted() {
        Order order = new Order();
        order.setId(1L);
        order.setQuantity(5);
        order.setFulfilledQuantity(5);

        StockMovement stock = new StockMovement();
        stock.setId(2L);
        stock.setQuantity(10);

        orderAllocationService.fulfillOrderWithStockMovement(order, stock);

        verifyNoInteractions(orderRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    public void fulfill_shouldOnlyUseNeededQuantityAndCompleteOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setQuantity(10);
        order.setFulfilledQuantity(8);
        order.setUser(new User());

        StockMovement stock = new StockMovement();
        stock.setId(2L);
        stock.setQuantity(50);

        orderAllocationService.fulfillOrderWithStockMovement(order, stock);

        assertEquals(10, order.getFulfilledQuantity());
        verify(orderRepository).save(order);
        verify(emailService).sendOrderCompleted(any(), eq(order));
    }
}
