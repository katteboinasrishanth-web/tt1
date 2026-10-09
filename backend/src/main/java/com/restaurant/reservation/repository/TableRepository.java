package com.restaurant.reservation.repository;

import com.restaurant.reservation.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {
    boolean existsByTableNumber(String tableNumber);
}