package com.sistemasweb.back_end_barber.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private Long id;
    private LocalDate date;
    private String time; // Formatted as "HH:MM - HH:MM"
    private LocalTime startTime;
    private LocalTime endTime;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long serviceId;
    private String serviceName;
    private BigDecimal servicePrice;
    private Long barberId;
    private String barberName;
    private String status;
    private String paymentId;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
}

