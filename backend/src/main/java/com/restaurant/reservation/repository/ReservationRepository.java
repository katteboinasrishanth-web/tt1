package com.restaurant.reservation.repository;

import com.restaurant.reservation.model.Reservation;
import com.restaurant.reservation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserOrderByReservationDateDesc(User user);

    List<Reservation> findByReservationDateOrderByReservationTime(LocalDate date);

    List<Reservation> findByReservationDateAndStatusIn(LocalDate date, List<String> statuses);
}