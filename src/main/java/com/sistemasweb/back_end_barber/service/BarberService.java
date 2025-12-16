package com.sistemasweb.back_end_barber.service;

import com.sistemasweb.back_end_barber.dto.BarberRequest;
import com.sistemasweb.back_end_barber.dto.BarberResponse;
import com.sistemasweb.back_end_barber.model.Barber;
import com.sistemasweb.back_end_barber.repository.BarberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BarberService {

    private final BarberRepository barberRepository;

    @Transactional(readOnly = true)
    public List<BarberResponse> getAllBarbers(Boolean active) {
        List<Barber> barbers;

        if (active != null) {
            barbers = barberRepository.findByActive(active);
        } else {
            barbers = barberRepository.findAll();
        }

        return barbers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BarberResponse getBarberById(Long id) {
        Barber barber = barberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barber not found with id: " + id));
        return mapToResponse(barber);
    }

    @Transactional
    public BarberResponse createBarber(BarberRequest request) {
        // Check if email already exists
        if (barberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Barber with email " + request.getEmail() + " already exists");
        }

        Barber barber = new Barber();
        barber.setName(request.getName());
        barber.setEmail(request.getEmail());
        barber.setPhone(request.getPhone());
        barber.setSpecialty(request.getSpecialty());
        barber.setActive(request.getActive() != null ? request.getActive() : true);
        barber.setCommissionPercentage(request.getCommissionPercentage());

        Barber savedBarber = barberRepository.save(barber);
        return mapToResponse(savedBarber);
    }

    @Transactional
    public BarberResponse updateBarber(Long id, BarberRequest request) {
        Barber barber = barberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barber not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (!barber.getEmail().equals(request.getEmail()) &&
            barberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Barber with email " + request.getEmail() + " already exists");
        }

        barber.setName(request.getName());
        barber.setEmail(request.getEmail());
        barber.setPhone(request.getPhone());
        barber.setSpecialty(request.getSpecialty());
        barber.setActive(request.getActive());
        barber.setCommissionPercentage(request.getCommissionPercentage());

        Barber updatedBarber = barberRepository.save(barber);
        return mapToResponse(updatedBarber);
    }

    @Transactional
    public void deleteBarber(Long id) {
        Barber barber = barberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barber not found with id: " + id));

        // Soft delete - just deactivate
        barber.setActive(false);
        barberRepository.save(barber);
    }

    private BarberResponse mapToResponse(Barber barber) {
        return BarberResponse.builder()
                .id(barber.getId())
                .name(barber.getName())
                .email(barber.getEmail())
                .phone(barber.getPhone())
                .specialty(barber.getSpecialty())
                .active(barber.getActive())
                .commissionPercentage(barber.getCommissionPercentage())
                .createdAt(barber.getCreatedAt())
                .build();
    }
}

