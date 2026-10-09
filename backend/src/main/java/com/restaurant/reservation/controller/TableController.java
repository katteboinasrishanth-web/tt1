package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.AvailabilityRequest;
import com.restaurant.reservation.dto.ComboSuggestionResponse;
import com.restaurant.reservation.exception.ApiException;
import com.restaurant.reservation.model.Occasion;
import com.restaurant.reservation.model.RestaurantTable;
import com.restaurant.reservation.service.ComboService;
import com.restaurant.reservation.service.TableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "http://localhost:5173")
public class TableController {

    private final TableService tableService;
    private final ComboService comboService;

    public TableController(TableService tableService, ComboService comboService) {
        this.tableService = tableService;
        this.comboService = comboService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<RestaurantTable>> available(AvailabilityRequest request) {
        return ResponseEntity.ok(tableService.findAvailable(request));
    }

    /**
     * Seating suggestions for a party: single tables plus combo suggestions where
     * two tables of the same zone are joined, best fit (fewest wasted seats) first.
     * The occasion is needed because its setup buffer is held before the slot.
     */
    @GetMapping("/combos")
    public ResponseEntity<List<ComboSuggestionResponse>> combos(AvailabilityRequest request,
                                                                @RequestParam(required = false) String occasion) {
        Occasion parsed;
        try {
            parsed = Occasion.parse(occasion);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(400, ex.getMessage());
        }
        return ResponseEntity.ok(comboService.suggest(request.getDate(), request.getTime(),
                request.getNumberOfPeople(), parsed));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantTable>> all() {
        return ResponseEntity.ok(tableService.allTables());
    }
}