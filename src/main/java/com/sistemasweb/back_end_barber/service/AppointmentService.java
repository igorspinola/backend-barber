package com.sistemasweb.back_end_barber.service;

import com.sistemasweb.back_end_barber.dto.AppointmentRequest;
import com.sistemasweb.back_end_barber.dto.AppointmentResponse;
import com.sistemasweb.back_end_barber.exception.ResourceNotFoundException;
import com.sistemasweb.back_end_barber.model.Appointment;
import com.sistemasweb.back_end_barber.model.Appointment.AppointmentStatus;
import com.sistemasweb.back_end_barber.model.Barber;
import com.sistemasweb.back_end_barber.model.User;
import com.sistemasweb.back_end_barber.repository.AppointmentRepository;
import com.sistemasweb.back_end_barber.repository.BarberRepository;
import com.sistemasweb.back_end_barber.repository.ServiceRepository;
import com.sistemasweb.back_end_barber.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BarberRepository barberRepository;

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments(LocalDate date, String status) {
        List<Appointment> appointments;

        if (date != null && status != null) {
            AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
            appointments = appointmentRepository.findByDateAndStatus(date, appointmentStatus);
        } else if (date != null) {
            appointments = appointmentRepository.findByDate(date);
        } else if (status != null) {
            AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
            appointments = appointmentRepository.findByStatus(appointmentStatus);
        } else {
            appointments = appointmentRepository.findAll();
        }

        return appointments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByUserId(Long userId) {
        List<Appointment> appointments = appointmentRepository.findByUserId(userId);
        return appointments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return convertToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // Validate user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        // Validate service
        var service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + request.getServiceId()));

        // Validate barber if provided
        Barber barber = null;
        if (request.getBarberId() != null) {
            barber = barberRepository.findById(request.getBarberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barber not found with id: " + request.getBarberId()));
        }

        // Check time slot availability
        boolean isAvailable = appointmentRepository.isTimeSlotAvailable(
                request.getDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getBarberId()
        );

        if (!isAvailable) {
            throw new IllegalArgumentException("The selected time slot is not available");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setDate(request.getDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setUser(user);
        appointment.setService(service);
        appointment.setBarber(barber);
        appointment.setServicePrice(service.getPrice());
        appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(savedAppointment);
    }

    @Transactional(readOnly = true)
    public boolean checkTimeSlotAvailability(LocalDate date, LocalTime startTime, LocalTime endTime, Long barberId) {
        return appointmentRepository.isTimeSlotAvailable(date, startTime, endTime, barberId);
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        AppointmentStatus newStatus;
        try {
            newStatus = AppointmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        appointment.setStatus(newStatus);

        // Update timestamp based on status
        switch (newStatus) {
            case CONFIRMED:
                appointment.setConfirmedAt(LocalDateTime.now());
                break;
            case COMPLETED:
                appointment.setCompletedAt(LocalDateTime.now());
                break;
            case CANCELLED:
                appointment.setCancelledAt(LocalDateTime.now());
                break;
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(updatedAppointment);
    }

    @Transactional
    public AppointmentResponse confirmAppointment(Long id, String paymentId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Only appointments with pending payment status can be confirmed");
        }

        appointment.setPaymentId(paymentId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setConfirmedAt(LocalDateTime.now());

        Appointment confirmedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(confirmedAppointment);
    }

    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    private AppointmentResponse convertToResponse(Appointment appointment) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = appointment.getStartTime().format(timeFormatter) + " - " +
                               appointment.getEndTime().format(timeFormatter);

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .date(appointment.getDate())
                .time(formattedTime)
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .userId(appointment.getUser().getId())
                .userName(appointment.getUser().getName())
                .userEmail(appointment.getUser().getEmail())
                .serviceId(appointment.getService().getId())
                .serviceName(appointment.getService().getName())
                .servicePrice(appointment.getServicePrice())
                .barberId(appointment.getBarber() != null ? appointment.getBarber().getId() : null)
                .barberName(appointment.getBarber() != null ? appointment.getBarber().getName() : null)
                .status(appointment.getStatus().name().toLowerCase())
                .paymentId(appointment.getPaymentId())
                .createdAt(appointment.getCreatedAt())
                .confirmedAt(appointment.getConfirmedAt())
                .cancelledAt(appointment.getCancelledAt())
                .completedAt(appointment.getCompletedAt())
                .build();
    }
}

