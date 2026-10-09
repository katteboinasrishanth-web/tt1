package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.AvailabilityRequest;
import com.restaurant.reservation.dto.TableRequest;
import com.restaurant.reservation.exception.ApiException;
import com.restaurant.reservation.model.RestaurantTable;
import com.restaurant.reservation.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableService {

    private static final List<String> VALID_STATUSES = List.of("AVAILABLE", "RESERVED", "MAINTENANCE");

    private final TableRepository tableRepository;
    private final ComboService comboService;

    public TableService(TableRepository tableRepository, ComboService comboService) {
        this.tableRepository = tableRepository;
        this.comboService = comboService;
    }

    public List<RestaurantTable> findAvailable(AvailabilityRequest req) {
        if (req.getDate() == null || req.getTime() == null || req.getNumberOfPeople() == null) {
            throw new ApiException(400, "date, time and numberOfPeople are required");
        }
        return comboService.freeTables(req.getDate(), TimeSlot.parse(req.getTime()), 0, null).stream()
                .filter(t -> t.getCapacity() >= req.getNumberOfPeople())
                .toList();
    }

    public List<RestaurantTable> allTables() {
        return tableRepository.findAll();
    }

    public RestaurantTable createTable(TableRequest req) {
        validate(req);
        if (tableRepository.existsByTableNumber(req.getTableNumber().trim())) {
            throw new ApiException(409, "Table number already exists");
        }
        return tableRepository.save(new RestaurantTable(req.getTableNumber().trim(),
                req.getCapacity(), req.getStatus().toUpperCase(), req.getZone()));
    }

    public RestaurantTable updateTable(Long id, TableRequest req) {
        validate(req);
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "Table not found"));
        table.setTableNumber(req.getTableNumber().trim());
        table.setCapacity(req.getCapacity());
        table.setStatus(req.getStatus().toUpperCase());
        table.setZone(req.getZone());
        return tableRepository.save(table);
    }

    public void deleteTable(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "Table not found"));
        tableRepository.delete(table);
    }

    private void validate(TableRequest req) {
        if (!VALID_STATUSES.contains(req.getStatus().toUpperCase())) {
            throw new ApiException(400, "Status must be one of: AVAILABLE, RESERVED, MAINTENANCE");
        }
    }
}