package com.diogobaptista.order_manager_api.service;

import com.diogobaptista.order_manager_api.dto.OrderRequestDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.entity.StockMovement;
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

    public Order create(OrderRequestDTO dto) {

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> {
                    String msg = "Order failed: Item not found with id=" + dto.getItemId();
                    logAndWrite(msg, "WARN");
                    return new NoSuchElementException(msg);
                });

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> {
                    String msg = "Order failed: User not found with id=" + dto.getUserId();
                    logAndWrite(msg, "WARN");
                    return new NoSuchElementException(msg);
                });

        Order order = new Order();
        order.setItem(item);
        order.setUser(user);
        order.setQuantity(dto.getQuantity());
        order.setCreationDate(LocalDateTime.now());

        Order savedOrder = repository.save(order);

        logAndWrite(
                String.format(
                        "Created Order [ID: %d, User: %s, Item: %s, Qty: %d]",
                        savedOrder.getId(),
                        user.getEmail(),
                        item.getName(),
                        savedOrder.getQuantity()
                ),
                "INFO"
        );

        int availableStock = item.getStockQuantity();

        if (availableStock <= 0) {
            logAndWrite(
                    String.format(
                            "Order %d created with no stock available [Item: %d, Requested: %d]",
                            savedOrder.getId(),
                            item.getId(),
                            savedOrder.getQuantity()
                    ),
                    "WARN"
            );
            return savedOrder;
        }

        int allocQty = Math.min(savedOrder.getQuantity(), availableStock);

        StockMovement stockMovement = new StockMovement();
        stockMovement.setItem(item);
        stockMovement.setQuantity(allocQty);
        stockMovement.setCreationDate(LocalDateTime.now());

        StockMovement savedMovement = stockRepo.save(stockMovement);

        item.setStockQuantity(availableStock - allocQty);
        itemRepository.save(item);

        logAndWrite(
                String.format(
                        "StockMovement %d created on order creation [Item: %d, Qty: %d]",
                        savedMovement.getId(),
                        item.getId(),
                        allocQty
                ),
                "INFO"
        );

        allocator.fulfillOrderWithStockMovement(savedOrder, savedMovement);

        if (allocQty == savedOrder.getQuantity()) {
            logAndWrite(
                    String.format(
                            "Order %d fully allocated on creation [Qty: %d]",
                            savedOrder.getId(),
                            allocQty
                    ),
                    "INFO"
            );
        } else {
            logAndWrite(
                    String.format(
                            "Order %d partially allocated on creation [Requested: %d, Allocated: %d, Remaining: %d]",
                            savedOrder.getId(),
                            savedOrder.getQuantity(),
                            allocQty,
                            savedOrder.getQuantity() - allocQty
                    ),
                    "INFO"
            );
        }

        return savedOrder;
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