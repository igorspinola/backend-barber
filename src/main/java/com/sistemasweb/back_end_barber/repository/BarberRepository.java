package com.sistemasweb.back_end_barber.repository;

import com.sistemasweb.back_end_barber.model.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {
    Optional<Barber> findByEmail(String email);
    List<Barber> findByActive(Boolean active);
    boolean existsByEmail(String email);
}

