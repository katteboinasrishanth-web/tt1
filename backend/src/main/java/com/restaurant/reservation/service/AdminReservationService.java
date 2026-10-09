package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.PrepBoardResponse;
import com.restaurant.reservation.dto.PrepTaskResponse;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.exception.ApiException;
import com.restaurant.reservation.model.Reservation;
import com.restaurant.reservation.model.RestaurantTable;
import com.restaurant.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminReservationService {

    private static final List<String> VALID_STATUSES = List.of("PENDING", "CONFIRMED", "CANCELLED", "COMPLETED");
    private static final List<String> UPCOMING_STATUSES = List.of("PENDING", "CONFIRMED");

    private final ReservationRepository reservationRepository;
    private final SessionManager sessionManager;
    private final ComboService comboService;
    private final DiningWindowService diningWindowService;

    public AdminReservationService(ReservationRepository reservationRepository,
                                   SessionManager sessionManager,
                                   ComboService comboService,
                                   DiningWindowService diningWindowService) {
        this.reservationRepository = reservationRepository;
        this.sessionManager = sessionManager;
        this.comboService = comboService;
        this.diningWindowService = diningWindowService;
    }

    public List<ReservationResponse> allReservations(String token) {
        sessionManager.requireRole(token, "ADMIN");
        List<Reservation> reservations = reservationRepository.findAll();
        Map<Long, List<RestaurantTable>> tables = comboService.tablesFor(reservations);
        return reservations.stream()
                .map(r -> ReservationResponse.from(r, tables.get(r.getReservationId())))
                .sorted((a, b) -> {
                    int c = b.getReservationDate().compareTo(a.getReservationDate());
                    return c != 0 ? c : b.getReservationTime().compareTo(a.getReservationTime());
                })
                .toList();
    }

    public List<ReservationResponse> reservationsByDate(String token, LocalDate date) {
        sessionManager.requireRole(token, "ADMIN");
        List<Reservation> reservations = reservationRepository.findByReservationDateOrderByReservationTime(date);
        Map<Long, List<RestaurantTable>> tables = comboService.tablesFor(reservations);
        return reservations.stream()
                .map(r -> ReservationResponse.from(r, tables.get(r.getReservationId())))
                .toList();
    }

    public ReservationResponse updateStatus(String token, Long reservationId, Map<String, String> body) {
        sessionManager.requireRole(token, "ADMIN");
        String status = body.get("status");
        if (status == null || !VALID_STATUSES.contains(status.toUpperCase())) {
            throw new ApiException(400, "Status must be one of: " + VALID_STATUSES);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException(404, "Reservation not found"));

        // If confirming, re-verify every table of the booking (single or combo)
        // is still free for the whole window, including the occasion setup buffer.
        if (status.equalsIgnoreCase("CONFIRMED")
                && (reservation.getStatus().equals("PENDING") || reservation.getStatus().equals("CANCELLED"))) {
            Map<Long, List<RestaurantTable>> tables = comboService.tablesFor(List.of(reservation));
            List<RestaurantTable> bookedTables = tables.getOrDefault(reservation.getReservationId(),
                    List.of(reservation.getTable()));
            for (RestaurantTable table : bookedTables) {
                if (!"AVAILABLE".equals(table.getStatus())) {
                    throw new ApiException(409, "Table " + table.getTableNumber() + " is not in service");
                }
            }
            int start = TimeSlot.parse(reservation.getReservationTime());
            Set<Long> busy = diningWindowService.busyTableIds(reservation.getReservationDate(),
                    start, reservation.getSetupBufferMinutes(), reservation.getReservationId());
            for (RestaurantTable table : bookedTables) {
                if (busy.contains(table.getTableId())) {
                    throw new ApiException(409,
                            "Table " + table.getTableNumber() + " is already booked for this slot");
                }
            }
        }

        reservation.setStatus(status.toUpperCase());
        Reservation saved = reservationRepository.save(reservation);
        Map<Long, List<RestaurantTable>> tables = comboService.tablesFor(List.of(saved));
        return ReservationResponse.from(saved, tables.get(saved.getReservationId()));
    }

    /**
     * Prep board: every upcoming booking for a day, ordered by the moment the table
     * has to be ready (slot minus the occasion setup buffer).
     */
    public PrepBoardResponse prepBoard(String token, LocalDate date) {
        sessionManager.requireRole(token, "ADMIN");
        List<Reservation> reservations =
                reservationRepository.findByReservationDateAndStatusIn(date, UPCOMING_STATUSES);
        Map<Long, List<RestaurantTable>> tables = comboService.tablesFor(reservations);
        List<PrepTaskResponse> tasks = reservations.stream()
                .map(r -> PrepTaskResponse.from(r, tables.get(r.getReservationId())))
                .sorted(Comparator.comparing(PrepTaskResponse::getPrepReadyTime))
                .toList();
        return new PrepBoardResponse(date, diningWindowService.getDiningMinutes(), tasks);
    }
}