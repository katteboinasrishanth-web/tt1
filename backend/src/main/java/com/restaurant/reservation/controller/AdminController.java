package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.TableRequest;
import com.restaurant.reservation.model.RestaurantTable;
import com.restaurant.reservation.service.AdminReservationService;
import com.restaurant.reservation.service.TableService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.reservation.dto.PrepBoardResponse;
import com.restaurant.reservation.dto.ReservationResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final TableService tableService;
    private final AdminReservationService adminReservationService;
    private final com.restaurant.reservation.service.SessionManager sessionManager;

    public AdminController(TableService tableService, AdminReservationService adminReservationService,
                           com.restaurant.reservation.service.SessionManager sessionManager) {
        this.tableService = tableService;
        this.adminReservationService = adminReservationService;
        this.sessionManager = sessionManager;
    }

    // ---- Manage Tables ----

    @PostMapping("/tables")
    public ResponseEntity<RestaurantTable> addTable(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody TableRequest request) {
        requireAdmin(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(tableService.createTable(request));
    }

    @PutMapping("/tables/{id}")
    public ResponseEntity<RestaurantTable> updateTable(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id,
            @Valid @RequestBody TableRequest request) {
        requireAdmin(auth);
        return ResponseEntity.ok(tableService.updateTable(id, request));
    }

    @DeleteMapping("/tables/{id}")
    public ResponseEntity<Map<String, String>> deleteTable(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        requireAdmin(auth);
        tableService.deleteTable(id);
        return ResponseEntity.ok(Map.of("message", "Table deleted"));
    }

    // ---- Manage Reservations ----

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> allReservations(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(adminReservationService.allReservations(extractToken(auth)));
    }

    @GetMapping("/reservations/date")
    public ResponseEntity<List<ReservationResponse>> reservationsByDate(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(adminReservationService.reservationsByDate(extractToken(auth), date));
    }

    @PutMapping("/reservations/{id}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminReservationService.updateStatus(extractToken(auth), id, body));
    }

    /** Prep board: bookings ordered by the time each table has to be ready. */
    @GetMapping("/prep-board")
    public ResponseEntity<PrepBoardResponse> prepBoard(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(adminReservationService.prepBoard(extractToken(auth),
                date == null ? LocalDate.now() : date));
    }

    private void requireAdmin(String auth) {
        String token = extractToken(auth);
        if (token == null) {
            throw new com.restaurant.reservation.exception.ApiException(401, "Not authenticated");
        }
        sessionManager.requireRole(token, "ADMIN");
    }

    private String extractToken(String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}