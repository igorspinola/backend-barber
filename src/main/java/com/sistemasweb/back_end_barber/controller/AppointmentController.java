package com.sistemasweb.back_end_barber.controller;

import com.sistemasweb.back_end_barber.dto.AppointmentRequest;
import com.sistemasweb.back_end_barber.dto.AppointmentResponse;
import com.sistemasweb.back_end_barber.dto.AppointmentStatusRequest;
import com.sistemasweb.back_end_barber.dto.ConfirmAppointmentRequest;
import com.sistemasweb.back_end_barber.security.CustomUserDetailsService;
import com.sistemasweb.back_end_barber.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status) {
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments(date, status);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByUser(
            @PathVariable Long userId,
            Authentication authentication) {

        // Users can only see their own appointments unless they are admin
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            Long authenticatedUserId = userDetailsService.getUserIdFromAuthentication(authentication);
            if (!authenticatedUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByUserId(userId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(Authentication authentication) {
        Long userId = userDetailsService.getUserIdFromAuthentication(authentication);
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByUserId(userId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @PathVariable Long id,
            Authentication authentication) {

        AppointmentResponse appointment = appointmentService.getAppointmentById(id);

        // Users can only see their own appointments unless they are admin
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            Long authenticatedUserId = userDetailsService.getUserIdFromAuthentication(authentication);
            if (!authenticatedUserId.equals(appointment.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(appointment);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            Authentication authentication) {

        // Users can only create appointments for themselves unless they are admin
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            Long authenticatedUserId = userDetailsService.getUserIdFromAuthentication(authentication);
            if (!authenticatedUserId.equals(request.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        AppointmentResponse appointment = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Long barberId) {

        boolean available = appointmentService.checkTimeSlotAvailability(date, startTime, endTime, barberId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request,
            Authentication authentication) {

        // Check permissions
        AppointmentResponse existingAppointment = appointmentService.getAppointmentById(id);
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            Long authenticatedUserId = userDetailsService.getUserIdFromAuthentication(authentication);
            if (!authenticatedUserId.equals(existingAppointment.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        AppointmentResponse appointment = appointmentService.updateAppointmentStatus(id, request.getStatus());
        return ResponseEntity.ok(appointment);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<Map<String, Object>> confirmAppointment(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmAppointmentRequest request,
            Authentication authentication) {

        // Check permissions
        AppointmentResponse existingAppointment = appointmentService.getAppointmentById(id);
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            Long authenticatedUserId = userDetailsService.getUserIdFromAuthentication(authentication);
            if (!authenticatedUserId.equals(existingAppointment.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        AppointmentResponse appointment = appointmentService.confirmAppointment(id, request.getPaymentId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Appointment confirmed");
        response.put("appointment", appointment);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long id,
            Authentication authentication) {

        // Check permissions
        AppointmentResponse existingAppointment = appointmentService.getAppointmentById(id);
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            Long authenticatedUserId = userDetailsService.getUserIdFromAuthentication(authentication);
            if (!authenticatedUserId.equals(existingAppointment.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        appointmentService.cancelAppointment(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Appointment cancelled");

        return ResponseEntity.ok(response);
    }
}

