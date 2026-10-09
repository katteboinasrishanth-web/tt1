package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.ComboSuggestionResponse;
import com.restaurant.reservation.exception.ApiException;
import com.restaurant.reservation.model.Occasion;
import com.restaurant.reservation.model.Reservation;
import com.restaurant.reservation.model.RestaurantTable;
import com.restaurant.reservation.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds seating suggestions for a party: single tables plus "combo" suggestions,
 * where two tables of the same zone are joined into one big table. A combo is only
 * suggested when it wastes fewer seats than the closest single table, so the
 * customer is never pushed into a bigger table than they need.
 */
@Service
public class ComboService {

    private static final int MAX_COMBOS = 4;

    private final TableRepository tableRepository;
    private final DiningWindowService diningWindowService;

    public ComboService(TableRepository tableRepository, DiningWindowService diningWindowService) {
        this.tableRepository = tableRepository;
        this.diningWindowService = diningWindowService;
    }

    public List<ComboSuggestionResponse> suggest(LocalDate date, String time, Integer people, Occasion occasion) {
        if (date == null || time == null || time.isBlank() || people == null) {
            throw new ApiException(400, "date, time and numberOfPeople are required");
        }
        if (people < 1) {
            throw new ApiException(400, "At least 1 person is required");
        }
        Occasion occ = occasion == null ? Occasion.NONE : occasion;
        int buffer = occ.getSetupBufferMinutes();
        int start = TimeSlot.parse(time);
        String prepReady = TimeSlot.format(start - buffer);

        List<RestaurantTable> free = freeTables(date, start, buffer, null);

        List<ComboSuggestionResponse> singles = free.stream()
                .filter(t -> t.getCapacity() >= people)
                .sorted(Comparator.comparingInt(RestaurantTable::getCapacity))
                .map(t -> ComboSuggestionResponse.single(t, people, buffer, prepReady))
                .toList();

        int bestSingleWaste = singles.isEmpty() ? Integer.MAX_VALUE : singles.get(0).getWastedSeats();

        List<ComboSuggestionResponse> combos = buildCombos(free, people, buffer, prepReady, bestSingleWaste)
                .stream()
                .sorted(Comparator.comparingInt(ComboSuggestionResponse::getWastedSeats))
                .limit(MAX_COMBOS)
                .toList();

        List<ComboSuggestionResponse> all = new ArrayList<>(combos);
        all.addAll(singles);
        if (all.isEmpty()) {
            throw new ApiException(409, nothingFitsMessage(free, people, date, time));
        }
        all.sort(Comparator.comparingInt(ComboSuggestionResponse::getWastedSeats)
                .thenComparing(ComboSuggestionResponse::getSeats));
        return all;
    }

    /** Nothing suggested means even the biggest joinable pair is too small for this party. */
    private String nothingFitsMessage(List<RestaurantTable> free, int people, LocalDate date, String time) {
        RestaurantTable bestSingle = free.stream()
                .max(Comparator.comparingInt(RestaurantTable::getCapacity))
                .orElse(null);
        RestaurantTable partner = null;
        RestaurantTable best = null;
        int bestSeats = 0;
        for (int i = 0; i < free.size(); i++) {
            for (int j = i + 1; j < free.size(); j++) {
                RestaurantTable a = free.get(i);
                RestaurantTable b = free.get(j);
                if (!sameZone(a, b)) {
                    continue;
                }
                int seats = a.getCapacity() + b.getCapacity();
                if (seats > bestSeats) {
                    bestSeats = seats;
                    partner = a;
                    best = b;
                }
            }
        }
        StringBuilder msg = new StringBuilder("Nothing can seat " + people + " people at ")
                .append(time).append(" on ").append(date);
        if (best != null) {
            msg.append(". The biggest group we can join is Tables ")
                    .append(partner.getTableNumber()).append(" + ").append(best.getTableNumber())
                    .append(" (").append(bestSeats).append(" seats)");
        } else if (bestSingle != null) {
            msg.append(". Our largest table seats ").append(bestSingle.getCapacity()).append(" people");
        } else {
            msg.append(". No table is free at this slot");
        }
        return msg.append(". Try another time or a smaller party.").toString();
    }

    /** Tables that are in service and not blocked by any reservation window. */
    public List<RestaurantTable> freeTables(LocalDate date, int startMinute, int bufferMinutes,
                                            Long excludeReservationId) {
        Set<Long> busy = diningWindowService.busyTableIds(date, startMinute, bufferMinutes, excludeReservationId);
        return tableRepository.findAll().stream()
                .filter(t -> "AVAILABLE".equals(t.getStatus()))
                .filter(t -> !busy.contains(t.getTableId()))
                .sorted(Comparator.comparingInt(RestaurantTable::getCapacity))
                .toList();
    }

    private List<ComboSuggestionResponse> buildCombos(List<RestaurantTable> free, int people, int buffer,
                                                      String prepReady, int bestSingleWaste) {
        List<ComboSuggestionResponse> combos = new ArrayList<>();
        for (int i = 0; i < free.size(); i++) {
            for (int j = i + 1; j < free.size(); j++) {
                RestaurantTable a = free.get(i);
                RestaurantTable b = free.get(j);
                if (!sameZone(a, b)) {
                    continue;
                }
                List<RestaurantTable> pair = List.of(a, b);
                int seats = a.getCapacity() + b.getCapacity();
                if (seats < people) {
                    continue;
                }
                if (a.getCapacity() >= people) {
                    // Table A alone is enough, joining tables would only waste seats.
                    continue;
                }
                ComboSuggestionResponse combo = ComboSuggestionResponse.combo(pair, people, buffer, prepReady);
                if (combo.getWastedSeats() >= bestSingleWaste) {
                    continue;
                }
                combo.setReason(comboReason(pair, people, seats, bestSingleWaste));
                combos.add(combo);
            }
        }
        return combos;
    }

    private String comboReason(List<RestaurantTable> pair, int people, int seats, int bestSingleWaste) {
        String zone = pair.get(0).getZone();
        String zoneText = (zone == null || zone.isBlank()) ? "" : " in the " + zone + " zone";
        if (bestSingleWaste == Integer.MAX_VALUE) {
            return "No single table seats " + people + " - joining these two" + zoneText
                    + " gives " + seats + " seats";
        }
        return "Joins " + seats + " seats" + zoneText + " for " + people + " people, "
                + (seats - people) + " spare instead of " + bestSingleWaste + " on the closest single table";
    }

    private boolean sameZone(RestaurantTable a, RestaurantTable b) {
        String za = a.getZone();
        String zb = b.getZone();
        boolean hasA = za != null && !za.isBlank();
        boolean hasB = zb != null && !zb.isBlank();
        if (!hasA || !hasB) {
            return false;
        }
        return za.equalsIgnoreCase(zb);
    }

    /**
     * Tables of each reservation (primary table first, then the joined tables),
     * falling back to the primary table for reservations booked before combos.
     */
    public Map<Long, List<RestaurantTable>> tablesFor(Collection<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return Map.of();
        }
        Map<Long, Set<Long>> extras = diningWindowService.comboTablesByReservation(reservations);

        Set<Long> wanted = new HashSet<>();
        for (Reservation r : reservations) {
            wanted.add(r.getTable().getTableId());
        }
        extras.values().forEach(wanted::addAll);

        Map<Long, RestaurantTable> byId = new HashMap<>();
        for (RestaurantTable t : tableRepository.findAllById(wanted)) {
            byId.put(t.getTableId(), t);
        }

        Map<Long, List<RestaurantTable>> result = new HashMap<>();
        for (Reservation r : reservations) {
            if (r.getReservationId() == null) {
                continue;
            }
            Set<Long> ids = new LinkedHashSet<>();
            ids.add(r.getTable().getTableId());
            ids.addAll(extras.getOrDefault(r.getReservationId(), Set.of()));
            List<RestaurantTable> tables = ids.stream()
                    .map(byId::get)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(RestaurantTable::getCapacity).reversed()
                            .thenComparing(RestaurantTable::getTableNumber))
                    .collect(Collectors.toList());
            result.put(r.getReservationId(), tables);
        }
        return result;
    }
}