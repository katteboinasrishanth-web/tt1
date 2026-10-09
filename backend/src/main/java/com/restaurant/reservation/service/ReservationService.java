package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.ReservationRequest;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.exception.ApiException;
import com.restaurant.reservation.model.Occasion;
import com.restaurant.reservation.model.Reservation;
import com.restaurant.reservation.model.ReservationTable;
import com.restaurant.reservation.model.RestaurantTable;
import com.restaurant.reservation.model.User;
import com.restaurant.reservation.repository.ReservationRepository;
import com.restaurant.reservation.repository.ReservationTableRepository;
import com.restaurant.reservation.repository.TableRepository;
import com.restaurant.reservation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTableRepository reservationTableRepository;
    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final DiningWindowService diningWindowService;
    private final ComboService comboService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTableRepository reservationTableRepository,
                              TableRepository tableRepository,
                              UserRepository userRepository,
                              SessionManager sessionManager,
                              DiningWindowService diningWindowService,
                              ComboService comboService) {
        this.reservationRepository = reservationRepository;
        this.reservationTableRepository = reservationTableRepository;
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
        this.diningWindowService = diningWindowService;
        this.comboService = comboService;
    }

    @Transactional
    public ReservationResponse create(String token, ReservationRequest req) {
        Long userId = sessionManager.requireRole(token, "CUSTOMER").id();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "User not found"));

        Occasion occasion = parseOccasion(req.getOccasion());
        int buffer = occasion.getSetupBufferMinutes();
        int start = TimeSlot.parse(req.getTime());
        int people = req.getNumberOfPeople();

        List<Long> requestedIds = req.resolveTableIds();
        if (requestedIds.isEmpty()) {
            throw new ApiException(400, "Select at least one table");
        }
        if (requestedIds.size() > 2) {
            throw new ApiException(400, "A booking can use at most 2 tables");
        }

        List<RestaurantTable> tables = new ArrayList<>();
        for (Long id : requestedIds) {
            tables.add(tableRepository.findById(id)
                    .orElseThrow(() -> new ApiException(404, "Table not found")));
        }

        for (RestaurantTable table : tables) {
            if (!"AVAILABLE".equals(table.getStatus())) {
                throw new ApiException(409, "Table " + table.getTableNumber() + " is not in service");
            }
        }

        // Second availability check: the table may have been booked while the customer
        // was completing the booking, so verify the whole window again before saving.
        var busy = diningWindowService.busyTableIds(req.getDate(), start, buffer, null);
        for (RestaurantTable table : tables) {
            if (busy.contains(table.getTableId())) {
                throw new ApiException(409,
                        "Sorry, table " + table.getTableNumber() + " was just booked for "
                                + req.getDate() + " at " + req.getTime() + ". Please choose another table.");
            }
        }

        int seats = tables.stream().mapToInt(RestaurantTable::getCapacity).sum();
        if (seats < people) {
            throw new ApiException(409, seats + " seats are not enough for " + people + " people");
        }
        if (tables.size() > 1) {
            validateCombo(tables, people);
        }

        // anchor table = biggest one, keeps the old single table columns meaningful
        RestaurantTable anchor = tables.stream()
                .max(Comparator.comparingInt(RestaurantTable::getCapacity))
                .orElseThrow();

        Reservation reservation = new Reservation(user, anchor, req.getDate(),
                req.getTime(), people, "PENDING");
        reservation.setOccasion(occasion);
        reservation.setOccasionNote(normaliseNote(req.getOccasionNote()));
        Reservation saved = reservationRepository.save(reservation);

        for (RestaurantTable table : tables) {
            reservationTableRepository.save(new ReservationTable(saved, table));
        }
        return ReservationResponse.from(saved, tables);
    }

    public List<ReservationResponse> myReservations(String token) {
        Long userId = sessionManager.requireRole(token, "CUSTOMER").id();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "User not found"));
        List<Reservation> reservations = reservationRepository.findByUserOrderByReservationDateDesc(user);
        Map<Long, List<RestaurantTable>> tables = comboService.tablesFor(reservations);
        return reservations.stream()
                .map(r -> ReservationResponse.from(r, tables.get(r.getReservationId())))
                .toList();
    }

    @Transactional
    public ReservationResponse cancelOwn(String token, Long reservationId) {
        Long userId = sessionManager.requireRole(token, "CUSTOMER").id();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException(404, "Reservation not found"));
        if (!reservation.getUser().getUserId().equals(userId)) {
            throw new ApiException(403, "You can only cancel your own reservations");
        }
        if (reservation.getStatus().equals("CONFIRMED") || reservation.getStatus().equals("PENDING")) {
            reservation.setStatus("CANCELLED");
        } else {
            throw new ApiException(400, "Reservation already " + reservation.getStatus().toLowerCase());
        }
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    private void validateCombo(List<RestaurantTable> tables, int people) {
        RestaurantTable first = tables.get(0);
        RestaurantTable second = tables.get(1);
        if (first.getZone() == null || first.getZone().isBlank()
                || !first.getZone().equalsIgnoreCase(second.getZone())) {
            throw new ApiException(400, "Tables can only be joined when they belong to the same zone");
        }
        if (first.getCapacity() >= people) {
            throw new ApiException(400, "Table " + first.getTableNumber()
                    + " already seats " + people + " people, no need to join tables");
        }
    }

    private Occasion parseOccasion(String value) {
        try {
            return Occasion.parse(value);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(400, ex.getMessage());
        }
    }

    private String normaliseNote(String note) {
        if (note == null) {
            return null;
        }
        String trimmed = note.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}