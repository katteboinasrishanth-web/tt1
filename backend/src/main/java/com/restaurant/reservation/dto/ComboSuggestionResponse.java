package com.restaurant.reservation.dto;

import com.restaurant.reservation.model.RestaurantTable;

import java.util.List;

/**
 * One seating suggestion for a search: either a single table or a combo of two
 * tables joined together, plus the reasoning shown to the customer.
 */
public class ComboSuggestionResponse {

    private String kind;
    private String label;
    private List<Long> tableIds;
    private List<String> tableNumbers;
    private Integer seats;
    private Integer wastedSeats;
    private String zone;
    private String reason;
    private Integer setupBufferMinutes;
    private String prepReadyTime;

    public static ComboSuggestionResponse single(RestaurantTable table, int people,
                                                 int bufferMinutes, String prepReadyTime) {
        ComboSuggestionResponse dto = new ComboSuggestionResponse();
        dto.kind = "SINGLE";
        dto.label = "Table " + table.getTableNumber();
        dto.tableIds = List.of(table.getTableId());
        dto.tableNumbers = List.of(table.getTableNumber());
        dto.seats = table.getCapacity();
        dto.wastedSeats = table.getCapacity() - people;
        dto.zone = table.getZone();
        dto.reason = table.getCapacity() == people
                ? "Exact fit for " + people + (people == 1 ? " person" : " people")
                : table.getCapacity() - people + " spare seat" + (table.getCapacity() - people == 1 ? "" : "s");
        dto.setupBufferMinutes = bufferMinutes;
        dto.prepReadyTime = prepReadyTime;
        return dto;
    }

    public static ComboSuggestionResponse combo(List<RestaurantTable> tables, int people,
                                                int bufferMinutes, String prepReadyTime) {
        ComboSuggestionResponse dto = new ComboSuggestionResponse();
        dto.kind = "COMBO";
        dto.tableIds = tables.stream().map(RestaurantTable::getTableId).toList();
        dto.tableNumbers = tables.stream().map(RestaurantTable::getTableNumber).toList();
        dto.label = "Tables " + String.join(" + ", dto.tableNumbers);
        dto.seats = tables.stream().mapToInt(RestaurantTable::getCapacity).sum();
        dto.wastedSeats = dto.seats - people;
        dto.zone = tables.isEmpty() ? null : tables.get(0).getZone();
        dto.setupBufferMinutes = bufferMinutes;
        dto.prepReadyTime = prepReadyTime;
        return dto;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getKind() {
        return kind;
    }

    public String getLabel() {
        return label;
    }

    public List<Long> getTableIds() {
        return tableIds;
    }

    public List<String> getTableNumbers() {
        return tableNumbers;
    }

    public Integer getSeats() {
        return seats;
    }

    public Integer getWastedSeats() {
        return wastedSeats;
    }

    public String getZone() {
        return zone;
    }

    public String getReason() {
        return reason;
    }

    public Integer getSetupBufferMinutes() {
        return setupBufferMinutes;
    }

    public String getPrepReadyTime() {
        return prepReadyTime;
    }
}