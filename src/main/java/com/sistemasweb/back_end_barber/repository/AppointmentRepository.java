package com.sistemasweb.back_end_barber.repository;

import com.sistemasweb.back_end_barber.model.Appointment;
import com.sistemasweb.back_end_barber.model.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDate(LocalDate date);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByDateAndStatus(LocalDate date, AppointmentStatus status);

    List<Appointment> findByUserId(Long userId);

    List<Appointment> findByBarberId(Long barberId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN false ELSE true END FROM Appointment a " +
           "WHERE a.date = :date " +
           "AND a.status != 'CANCELLED' " +
           "AND ((:barberId IS NULL) OR (a.barber.id = :barberId)) " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    boolean isTimeSlotAvailable(@Param("date") LocalDate date,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime,
                                @Param("barberId") Long barberId);

    List<Appointment> findByDateBetween(LocalDate startDate, LocalDate endDate);
}

