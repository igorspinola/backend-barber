package com.sistemasweb.back_end_barber.controller;

import com.sistemasweb.back_end_barber.dto.BarberRequest;
import com.sistemasweb.back_end_barber.dto.BarberResponse;
import com.sistemasweb.back_end_barber.service.BarberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/barbers")
@RequiredArgsConstructor
public class BarberController {

    private final BarberService barberService;

    @GetMapping
    public ResponseEntity<List<BarberResponse>> getAllBarbers(
            @RequestParam(required = false) Boolean active) {
        List<BarberResponse> barbers = barberService.getAllBarbers(active);
        return ResponseEntity.ok(barbers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberResponse> getBarberById(@PathVariable Long id) {
        try {
            BarberResponse barber = barberService.getBarberById(id);
            return ResponseEntity.ok(barber);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBarber(@Valid @RequestBody BarberRequest request) {
        try {
            BarberResponse barber = barberService.createBarber(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(barber);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBarber(
            @PathVariable Long id,
            @Valid @RequestBody BarberRequest request) {
        try {
            BarberResponse barber = barberService.updateBarber(id, request);
            return ResponseEntity.ok(barber);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBarber(@PathVariable Long id) {
        try {
            barberService.deleteBarber(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Barber deactivated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}

