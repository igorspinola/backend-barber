package com.sistemasweb.back_end_barber.service;

import com.sistemasweb.back_end_barber.dto.ServiceRequest;
import com.sistemasweb.back_end_barber.dto.ServiceResponse;
import com.sistemasweb.back_end_barber.model.Service;
import com.sistemasweb.back_end_barber.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllServices(Boolean active, String category) {
        List<Service> services;

        if (active != null && category != null) {
            Service.ServiceCategory serviceCategory = Service.ServiceCategory.valueOf(category.toUpperCase());
            services = serviceRepository.findByActiveAndCategory(active, serviceCategory);
        } else if (active != null) {
            services = serviceRepository.findByActive(active);
        } else if (category != null) {
            Service.ServiceCategory serviceCategory = Service.ServiceCategory.valueOf(category.toUpperCase());
            services = serviceRepository.findByCategory(serviceCategory);
        } else {
            services = serviceRepository.findAll();
        }

        return services.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        return mapToResponse(service);
    }

    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        Service service = new Service();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDuration(request.getDuration());
        service.setCategory(Service.ServiceCategory.valueOf(request.getCategory().toUpperCase()));
        service.setActive(request.getActive() != null ? request.getActive() : true);
        service.setBarbershopId(request.getBarbershopId());
        service.setImageUrl(request.getImageUrl());

        Service savedService = serviceRepository.save(service);
        return mapToResponse(savedService);
    }

    @Transactional
    public ServiceResponse updateService(Long id, ServiceRequest request) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDuration(request.getDuration());
        service.setCategory(Service.ServiceCategory.valueOf(request.getCategory().toUpperCase()));
        service.setActive(request.getActive());
        service.setBarbershopId(request.getBarbershopId());
        service.setImageUrl(request.getImageUrl());

        Service updatedService = serviceRepository.save(service);
        return mapToResponse(updatedService);
    }

    @Transactional
    public void deleteService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));

        serviceRepository.delete(service);
    }

    private ServiceResponse mapToResponse(Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .duration(service.getDuration())
                .category(service.getCategory().name().toLowerCase())
                .active(service.getActive())
                .barbershopId(service.getBarbershopId())
                .imageUrl(service.getImageUrl())
                .build();
    }
}

