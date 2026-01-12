package com.diogobaptista.order_manager_api.service;

import com.diogobaptista.order_manager_api.dto.StockMovementRequestDTO;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockMovementService {

    private static final Logger log = LoggerFactory.getLogger(StockMovementService.class);
    private final StockMovementRepository repository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderAllocationService orderAllocationService;
    private final FileLogService fileLogService; // Injeção do serviço de log
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
        return itemRepository.findById(dto.getItemId())
                .map(item -> {
                    List<Order> pendingOrders = orderRepository.findAll().stream()
                            .filter(o -> !o.isComplete())
                            .filter(o -> o.getItem().getId().equals(item.getId()))
                            .collect(Collectors.toList());

                    if (pendingOrders.isEmpty()) {
                        logAndWrite(String.format("StockMovement for Item %d - No pending orders found", item.getId()), "WARN");
                        return Optional.<StockMovement>empty();
                    }

                    int availableStock = item.getStockQuantity();
                    if (dto.getQuantity() > availableStock) {
                        logAndWrite(String.format("Fail StockMovement for Item %d - Insufficient stock (Req: %d, Avail: %d)",
                                item.getId(), dto.getQuantity(), availableStock), "WARN");
                        return Optional.<StockMovement>empty();
                    }

                    StockMovement stockMovement = mapper.toEntity(dto, item);
                    stockMovement.setCreationDate(LocalDateTime.now());
                    stockMovement.setQuantity(dto.getQuantity());
                    StockMovement saved = repository.save(stockMovement);

                    item.setStockQuantity(availableStock - dto.getQuantity());
                    itemRepository.save(item);

                    logAndWrite(String.format("Create StockMovement %d [Item: %d, Qty: %d]",
                            saved.getId(), item.getId(), dto.getQuantity()), "INFO");

                    pendingOrders.forEach(order -> orderAllocationService.fulfillOrderWithStockMovement(order, saved));

                    return Optional.of(saved);
                }).orElseGet(() -> {
                    logAndWrite(String.format("Fail StockMovement creation - Item %d not found", dto.getItemId()), "WARN");
                    return Optional.empty();
                });
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
