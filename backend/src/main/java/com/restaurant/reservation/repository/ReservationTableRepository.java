package com.restaurant.reservation.repository;

import com.restaurant.reservation.model.ReservationTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReservationTableRepository extends JpaRepository<ReservationTable, Long> {

    @Query("SELECT rt FROM ReservationTable rt WHERE rt.reservation.reservationId IN :ids")
    List<ReservationTable> findByReservationIds(@Param("ids") Collection<Long> reservationIds);

    @Query("SELECT rt FROM ReservationTable rt WHERE rt.reservation.reservationId = :id")
    List<ReservationTable> findByReservationId(@Param("id") Long reservationId);
}