package com.restaurant.reservation.model;

import jakarta.persistence.*;

/**
 * Link row for multi-table (combo) bookings. A single-table booking also gets one
 * row so the reservation always has a single source of truth for its tables.
 */
@Entity
@Table(name = "reservation_tables",
        uniqueConstraints = @UniqueConstraint(name = "uk_reservation_table",
                columnNames = {"reservation_id", "table_id"}))
public class ReservationTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    public ReservationTable() {
    }

    public ReservationTable(Reservation reservation, RestaurantTable table) {
        this.reservation = reservation;
        this.table = table;
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public RestaurantTable getTable() {
        return table;
    }

    public void setTable(RestaurantTable table) {
        this.table = table;
    }
}