package com.diogobaptista.order_manager_api.service;

import com.diogobaptista.order_manager_api.dto.OrderRequestDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.entity.User;
import com.diogobaptista.order_manager_api.repository.ItemRepository;
import com.diogobaptista.order_manager_api.repository.OrderRepository;
import com.diogobaptista.order_manager_api.repository.StockMovementRepository;
import com.diogobaptista.order_manager_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final StockMovementRepository stockRepo;
    private final OrderAllocationService allocator;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final FileLogService fileLogService;

    public OrderService(OrderRepository repository,
                        StockMovementRepository stockRepo,
                        OrderAllocationService allocator,
                        ItemRepository itemRepository,
                        UserRepository userRepository,
                        FileLogService fileLogService) {
        this.repository = repository;
        this.stockRepo = stockRepo;
        this.allocator = allocator;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.fileLogService = fileLogService;
    }

    public List<Order> findAll() {
        return repository.findAll();
    }

    public Optional<Order> findById(Long id) {
        return repository.findById(id);
    }

    public Order create(OrderRequestDTO orderRequestDTO) {
        try {
            Item item = itemRepository.findById(orderRequestDTO.getItemId())
                    .orElseThrow(() -> {
                        String msg = "Order failed: Item not found with id=" + orderRequestDTO.getItemId();
                        logAndWrite(msg, "WARN");
                        return new NoSuchElementException(msg);
                    });

            User user = userRepository.findById(orderRequestDTO.getUserId())
                    .orElseThrow(() -> {
                        String msg = "Order failed: User not found with id=" + orderRequestDTO.getUserId();
                        logAndWrite(msg, "WARN");
                        return new NoSuchElementException(msg);
                    });

            Order order = new Order();
            order.setQuantity(orderRequestDTO.getQuantity());
            order.setItem(item);
            order.setUser(user);
            order.setCreationDate(LocalDateTime.now());

            Order savedOrder = repository.save(order);

            logAndWrite(String.format("Created Order [ID: %d, User: %s, Item: %s, Qty: %d]",
                    savedOrder.getId(), user.getEmail(), item.getName(), order.getQuantity()), "INFO");

            return savedOrder;

        } catch (NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            logAndWrite("UNEXPECTED ERROR creating order: " + e.getMessage(), "ERROR");
            throw e;
        }
    }

    private void logAndWrite(String message, String level) {
        if ("WARN".equals(level)) {
            logger.warn(message);
        } else if ("ERROR".equals(level)) {
            logger.error(message);
        } else {
            logger.info(message);
        }
        fileLogService.appendLine(message);
    }
}