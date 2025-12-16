package com.sistemasweb.back_end_barber.repository;

import com.sistemasweb.back_end_barber.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByActive(Boolean active);
    List<Service> findByCategory(Service.ServiceCategory category);
    List<Service> findByActiveAndCategory(Boolean active, Service.ServiceCategory category);
}

