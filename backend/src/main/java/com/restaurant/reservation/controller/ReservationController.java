package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.ReservationRequest;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:5173")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.create(extractToken(auth), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationResponse>> mine(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(reservationService.myReservations(extractToken(auth)));
    }

    @DeleteMapping("/mine/{id}")
    public ResponseEntity<ReservationResponse> cancel(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelOwn(extractToken(auth), id));
    }

    private String extractToken(String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}