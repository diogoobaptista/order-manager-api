package com.diogobaptista.order_manager_api.repository;

import com.diogobaptista.order_manager_api.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {}


