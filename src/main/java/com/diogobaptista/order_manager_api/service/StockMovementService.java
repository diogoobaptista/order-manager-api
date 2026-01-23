package com.diogobaptista.order_manager_api.service;

import com.diogobaptista.order_manager_api.dto.StockMovementRequestDTO;
import com.diogobaptista.order_manager_api.entity.Item;
import com.diogobaptista.order_manager_api.entity.Order;
import com.diogobaptista.order_manager_api.entity.StockMovement;
import com.diogobaptista.order_manager_api.mapper.StockMovementMapper;
import com.diogobaptista.order_manager_api.repository.OrderRepository;
import com.diogobaptista.order_manager_api.repository.StockMovementRepository;
import com.diogobaptista.order_manager_api.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockMovementService {

    private static final Logger log = LoggerFactory.getLogger(StockMovementService.class);
    private final StockMovementRepository repository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderAllocationService orderAllocationService;
    private final FileLogService fileLogService;
    private final StockMovementMapper mapper;

    public StockMovementService(StockMovementRepository repository,
                                ItemRepository itemRepository,
                                OrderRepository orderRepository,
                                OrderAllocationService orderAllocationService,
                                FileLogService fileLogService,
                                StockMovementMapper mapper) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.orderAllocationService = orderAllocationService;
        this.fileLogService = fileLogService;
        this.mapper = mapper;
    }

    public List<StockMovement> findAll() {
        return repository.findAll();
    }

    public Optional<StockMovement> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<StockMovement> createStockMovement(StockMovementRequestDTO dto) {

        Order order = getOrder(dto.getOrderId());
        Item item = order.getItem();

        validateOrder(order);
        int remainingQty = getRemainingQuantity(order);
        validateRequestedQuantity(dto.getQuantity(), remainingQty, order);

        int availableStock = getAvailableStock(item);
        int allocQty = calculateAllocation(dto.getQuantity(), remainingQty, availableStock);

        StockMovement stockMovement = mapper.toEntity(dto, item);
        stockMovement.setQuantity(allocQty);
        stockMovement.setCreationDate(LocalDateTime.now());

        StockMovement saved = repository.save(stockMovement);

        updateItemStock(item, availableStock, allocQty);
        orderAllocationService.fulfillOrderWithStockMovement(order, saved);

        logAndWrite(
                String.format("StockMovement %d allocated to Order %d [Qty: %d]", saved.getId(), order.getId(), allocQty),
                "INFO"
        );

        return Optional.of(saved);
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    private void validateOrder(Order order) {
        if (order.isComplete()) {
            logAndWrite(String.format("Order %d already completed", order.getId()), "WARN");
            throw new IllegalStateException("Order already completed");
        }
    }

    private int getRemainingQuantity(Order order) {
        return order.getQuantity() - order.getFulfilledQuantity();
    }

    private void validateRequestedQuantity(int requestedQty, int remainingQty, Order order) {
        if (requestedQty > remainingQty) {
            logAndWrite(
                    String.format("Invalid StockMovement request for Order %d - Requested: %d, Remaining: %d",
                            order.getId(), requestedQty, remainingQty),
                    "WARN"
            );
            throw new IllegalArgumentException("Requested quantity exceeds remaining quantity");
        }
    }

    private int getAvailableStock(Item item) {
        int stock = item.getStockQuantity();
        if (stock <= 0) {
            logAndWrite(String.format("No stock available for Item %d", item.getId()), "WARN");
            throw new IllegalStateException("No stock available");
        }
        return stock;
    }

    private int calculateAllocation(int requestedQty, int remainingQty, int availableStock) {
        return Math.min(requestedQty, Math.min(remainingQty, availableStock));
    }

    private void updateItemStock(Item item, int availableStock, int allocQty) {
        item.setStockQuantity(availableStock - allocQty);
        itemRepository.save(item);
    }

    private void logAndWrite(String message, String level) {
        if ("WARN".equals(level)) {
            log.warn(message);
        } else {
            log.info(message);
        }
        fileLogService.appendLine(message);
    }
}
