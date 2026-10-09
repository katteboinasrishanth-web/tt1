package com.restaurant.reservation.dto;

import com.restaurant.reservation.model.Occasion;
import com.restaurant.reservation.model.Reservation;
import com.restaurant.reservation.model.RestaurantTable;
import com.restaurant.reservation.service.TimeSlot;

import java.time.LocalDate;
import java.util.List;

/**
 * One line on the admin "prep board": everything the floor team needs before the
 * guests of an occasion booking arrive.
 */
public class PrepTaskResponse {

    private Long reservationId;
    private String customerName;
    private String customerPhone;
    private LocalDate date;
    private String time;
    private String prepReadyTime;
    private List<String> tableNumbers;
    private boolean combo;
    private Integer numberOfPeople;
    private Integer totalSeats;
    private String occasion;
    private String occasionLabel;
    private String occasionNote;
    private Integer setupBufferMinutes;
    private String status;

    public static PrepTaskResponse from(Reservation r, List<RestaurantTable> tables) {
        PrepTaskResponse dto = new PrepTaskResponse();
        List<RestaurantTable> reserved = (tables == null || tables.isEmpty())
                ? List.of(r.getTable()) : tables;

        Occasion occasion = r.getOccasion() == null ? Occasion.NONE : r.getOccasion();
        dto.reservationId = r.getReservationId();
        dto.customerName = r.getUser().getName();
        dto.customerPhone = r.getUser().getPhone();
        dto.date = r.getReservationDate();
        dto.time = r.getReservationTime();
        dto.prepReadyTime = TimeSlot.format(TimeSlot.parse(r.getReservationTime())
                - occasion.getSetupBufferMinutes());
        dto.tableNumbers = reserved.stream().map(RestaurantTable::getTableNumber).toList();
        dto.combo = reserved.size() > 1;
        dto.numberOfPeople = r.getNumberOfPeople();
        dto.totalSeats = reserved.stream().mapToInt(RestaurantTable::getCapacity).sum();
        dto.occasion = occasion.name();
        dto.occasionLabel = occasion.getLabel();
        dto.occasionNote = r.getOccasionNote();
        dto.setupBufferMinutes = occasion.getSetupBufferMinutes();
        dto.status = r.getStatus();
        return dto;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getPrepReadyTime() {
        return prepReadyTime;
    }

    public List<String> getTableNumbers() {
        return tableNumbers;
    }

    public boolean isCombo() {
        return combo;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public String getOccasion() {
        return occasion;
    }

    public String getOccasionLabel() {
        return occasionLabel;
    }

    public String getOccasionNote() {
        return occasionNote;
    }

    public Integer getSetupBufferMinutes() {
        return setupBufferMinutes;
    }

    public String getStatus() {
        return status;
    }
}