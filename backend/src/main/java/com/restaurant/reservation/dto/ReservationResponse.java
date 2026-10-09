package com.restaurant.reservation.dto;

import com.restaurant.reservation.model.Occasion;
import com.restaurant.reservation.model.User;
import com.restaurant.reservation.model.Reservation;
import com.restaurant.reservation.model.RestaurantTable;
import com.restaurant.reservation.service.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationResponse {

    private Long reservationId;
    private Long tableId;
    private String tableNumber;
    private Integer capacity;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private LocalDate reservationDate;
    private String reservationTime;
    private Integer numberOfPeople;
    private String status;
    private LocalDateTime createdAt;

    // combo booking
    private boolean combo;
    private List<Long> tableIds;
    private List<String> tableNumbers;
    private Integer totalSeats;

    // occasion mode
    private String occasion;
    private String occasionLabel;
    private String occasionNote;
    private Integer setupBufferMinutes;
    private String prepReadyTime;

    public static ReservationResponse from(Reservation r) {
        return from(r, List.of(r.getTable()));
    }

    public static ReservationResponse from(Reservation r, List<RestaurantTable> tables) {
        ReservationResponse dto = new ReservationResponse();
        dto.reservationId = r.getReservationId();

        List<RestaurantTable> reserved = (tables == null || tables.isEmpty())
                ? List.of(r.getTable()) : tables;

        RestaurantTable t = r.getTable();
        dto.tableId = t.getTableId();
        dto.tableNumber = t.getTableNumber();
        dto.capacity = t.getCapacity();

        dto.tableIds = reserved.stream().map(RestaurantTable::getTableId).toList();
        dto.tableNumbers = reserved.stream().map(RestaurantTable::getTableNumber).toList();
        dto.totalSeats = reserved.stream().mapToInt(RestaurantTable::getCapacity).sum();
        dto.combo = reserved.size() > 1;

        User u = r.getUser();
        dto.customerName = u.getName();
        dto.customerEmail = u.getEmail();
        dto.customerPhone = u.getPhone();
        dto.reservationDate = r.getReservationDate();
        dto.reservationTime = r.getReservationTime();
        dto.numberOfPeople = r.getNumberOfPeople();
        dto.status = r.getStatus();
        dto.createdAt = r.getCreatedAt();

        Occasion occasion = r.getOccasion() == null ? Occasion.NONE : r.getOccasion();
        dto.occasion = occasion.name();
        dto.occasionLabel = occasion.getLabel();
        dto.occasionNote = r.getOccasionNote();
        dto.setupBufferMinutes = occasion.getSetupBufferMinutes();
        dto.prepReadyTime = TimeSlot.format(TimeSlot.parse(r.getReservationTime()) - occasion.getSetupBufferMinutes());
        return dto;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getTableId() {
        return tableId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isCombo() {
        return combo;
    }

    public List<Long> getTableIds() {
        return tableIds;
    }

    public List<String> getTableNumbers() {
        return tableNumbers;
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

    public String getPrepReadyTime() {
        return prepReadyTime;
    }
}