package com.diogobaptista.order_manager_api.service;

import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.entity.StockMovement;
import com.diogobaptista.order_manager_api.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderAllocationService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final FileLogService fileLogService;
    private final Logger log = LoggerFactory.getLogger(OrderAllocationService.class);

    public OrderAllocationService(OrderRepository orderRepository,
                                  EmailService emailService,
                                  FileLogService fileLogService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.fileLogService = fileLogService;
    }

    private void logAndWrite(String message) {
        log.info(message);
        fileLogService.appendLine(message);
    }

    public void fulfillOrderWithStockMovement(Order order, StockMovement stock) {
        int needed = order.getQuantity() - order.getFulfilledQuantity();
        int available = stock.getQuantity();
        int used = Math.min(needed, available);

        if (used <= 0) return;

        order.setFulfilledQuantity(order.getFulfilledQuantity() + used);
        orderRepository.save(order);

        logAndWrite(String.format("Allocated %d of StockMovement %d to Order %d", used, stock.getId(), order.getId()));

        if (order.isComplete()) {
            logAndWrite(String.format("Order %d COMPLETED", order.getId()));
            emailService.sendOrderCompleted(order.getUser(), order);
        }
    }
}