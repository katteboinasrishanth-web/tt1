package com.restaurant.reservation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationRequest {

    /** Single table booking (kept for the simple flow). */
    private Long tableId;

    /** Combo booking: two tables of the same zone joined into one big table. */
    private List<Long> tableIds = new ArrayList<>();

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date cannot be in the past")
    private LocalDate date;

    @NotBlank(message = "Time is required")
    private String time;

    @NotNull(message = "Number of people is required")
    @Min(value = 1, message = "At least 1 person is required")
    private Integer numberOfPeople;

    /** NONE, BIRTHDAY, ANNIVERSARY, DATE_NIGHT, BUSINESS, FAMILY, OTHER. */
    private String occasion = "NONE";

    @Size(max = 200, message = "Note can be at most 200 characters")
    private String occasionNote;

    /** Tables of this booking, whether the client sent tableId or tableIds. */
    public List<Long> resolveTableIds() {
        List<Long> ids = new ArrayList<>();
        if (tableIds != null) {
            for (Long id : tableIds) {
                if (id != null && !ids.contains(id)) {
                    ids.add(id);
                }
            }
        }
        if (tableId != null && !ids.contains(tableId)) {
            ids.add(tableId);
        }
        return ids;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public List<Long> getTableIds() {
        return tableIds;
    }

    public void setTableIds(List<Long> tableIds) {
        this.tableIds = tableIds;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(Integer numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getOccasion() {
        return occasion;
    }

    public void setOccasion(String occasion) {
        this.occasion = occasion;
    }

    public String getOccasionNote() {
        return occasionNote;
    }

    public void setOccasionNote(String occasionNote) {
        this.occasionNote = occasionNote;
    }
}