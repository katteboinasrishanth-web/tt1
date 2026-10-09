package com.restaurant.reservation.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Anchor table of the booking. For a combo booking this is the biggest table. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false, length = 10)
    private String reservationTime;

    @Column(name = "number_of_people", nullable = false)
    private Integer numberOfPeople;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Occasion occasion = Occasion.NONE;

    @Column(name = "occasion_note", length = 200)
    private String occasionNote;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Reservation() {
    }

    public Reservation(User user, RestaurantTable table, LocalDate reservationDate,
                       String reservationTime, Integer numberOfPeople, String status) {
        this.user = user;
        this.table = table;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.numberOfPeople = numberOfPeople;
        this.status = status;
    }

    /** Minutes the table is held before the slot for decorations, cake, pre-printing. */
    public int getSetupBufferMinutes() {
        return occasion == null ? 0 : occasion.getSetupBufferMinutes();
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RestaurantTable getTable() {
        return table;
    }

    public void setTable(RestaurantTable table) {
        this.table = table;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(String reservationTime) {
        this.reservationTime = reservationTime;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(Integer numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Occasion getOccasion() {
        return occasion;
    }

    public void setOccasion(Occasion occasion) {
        this.occasion = occasion == null ? Occasion.NONE : occasion;
    }

    public String getOccasionNote() {
        return occasionNote;
    }

    public void setOccasionNote(String occasionNote) {
        this.occasionNote = occasionNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}