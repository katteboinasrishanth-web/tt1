package com.restaurant.reservation.service;

import com.restaurant.reservation.model.Reservation;
import com.restaurant.reservation.model.ReservationTable;
import com.restaurant.reservation.repository.ReservationRepository;
import com.restaurant.reservation.repository.ReservationTableRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Decides whether a table is really free for a slot.
 *
 * A reservation is not a dot on a clock, it is a window: the table is held from
 * (slot - occasion setup buffer) until (slot + dining duration). Two windows that
 * touch each other clash, which is what stops two bookings an hour apart from
 * overlapping on a 90 minute meal.
 */
@Service
public class DiningWindowService {

    private static final List<String> ACTIVE_STATUSES = List.of("PENDING", "CONFIRMED");

    private final ReservationRepository reservationRepository;
    private final ReservationTableRepository reservationTableRepository;
    private final int diningMinutes;

    public DiningWindowService(ReservationRepository reservationRepository,
                               ReservationTableRepository reservationTableRepository,
                               @Value("${tt1.dining.duration-minutes:90}") int diningMinutes) {
        this.reservationRepository = reservationRepository;
        this.reservationTableRepository = reservationTableRepository;
        this.diningMinutes = diningMinutes;
    }

    public int getDiningMinutes() {
        return diningMinutes;
    }

    /**
     * Table ids that are already taken (single or part of a combo) for a booking
     * that starts at {@code startMinute} and needs {@code bufferMinutes} of setup.
     */
    public Set<Long> busyTableIds(LocalDate date, int startMinute, int bufferMinutes, Long excludeReservationId) {
        Set<Long> busy = new HashSet<>();
        for (Overlap overlap : overlapping(date, startMinute, bufferMinutes, excludeReservationId)) {
            busy.addAll(overlap.tables());
        }
        return busy;
    }

    /**
     * Reservations of that day whose window clashes with the requested one and that
     * hold at least one of {@code tableIds}. Used to explain a rejection properly.
     */
    public List<Reservation> conflictsFor(LocalDate date, int startMinute, int bufferMinutes,
                                          Collection<Long> tableIds, Long excludeReservationId) {
        if (tableIds == null || tableIds.isEmpty()) {
            return List.of();
        }
        Set<Long> wanted = new HashSet<>(tableIds);
        return overlapping(date, startMinute, bufferMinutes, excludeReservationId).stream()
                .filter(overlap -> !Collections.disjoint(overlap.tables(), wanted))
                .map(Overlap::reservation)
                .toList();
    }

    /** Human readable occupancy window of a reservation, e.g. 18:45 - 20:30. */
    public String windowOf(Reservation reservation) {
        int start;
        try {
            start = TimeSlot.parse(reservation.getReservationTime());
        } catch (RuntimeException ex) {
            return reservation.getReservationTime();
        }
        return TimeSlot.format(start - reservation.getSetupBufferMinutes())
                + " - " + TimeSlot.format(start + diningMinutes);
    }

    private List<Overlap> overlapping(LocalDate date, int startMinute, int bufferMinutes,
                                      Long excludeReservationId) {
        List<Reservation> sameDay =
                reservationRepository.findByReservationDateAndStatusIn(date, ACTIVE_STATUSES);
        if (sameDay.isEmpty()) {
            return List.of();
        }
        Map<Long, Set<Long>> combos = comboTablesByReservation(sameDay);
        int candidateStart = startMinute - bufferMinutes;
        int candidateEnd = startMinute + diningMinutes;

        List<Overlap> hits = new ArrayList<>();
        for (Reservation reservation : sameDay) {
            if (excludeReservationId != null && excludeReservationId.equals(reservation.getReservationId())) {
                continue;
            }
            int reservedStart;
            try {
                reservedStart = TimeSlot.parse(reservation.getReservationTime());
            } catch (RuntimeException ex) {
                continue;
            }
            int windowStart = reservedStart - reservation.getSetupBufferMinutes();
            int windowEnd = reservedStart + diningMinutes;
            if (windowStart >= candidateEnd || windowEnd <= candidateStart) {
                continue;
            }
            Set<Long> held = new HashSet<>(combos.getOrDefault(reservation.getReservationId(), Set.of()));
            held.add(reservation.getTable().getTableId());
            hits.add(new Overlap(reservation, held, windowStart, windowEnd));
        }
        return hits;
    }

    private record Overlap(Reservation reservation, Set<Long> tables, int windowStart, int windowEnd) {
    }

    /** Table ids per reservation id for the joined tables of each reservation. */
    public Map<Long, Set<Long>> comboTablesByReservation(Collection<Reservation> reservations) {
        List<Long> ids = reservations.stream()
                .map(Reservation::getReservationId)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<ReservationTable> links = reservationTableRepository.findByReservationIds(ids);
        return links.stream().collect(Collectors.groupingBy(
                link -> link.getReservation().getReservationId(),
                Collectors.mapping(link -> link.getTable().getTableId(), Collectors.toSet())));
    }
}