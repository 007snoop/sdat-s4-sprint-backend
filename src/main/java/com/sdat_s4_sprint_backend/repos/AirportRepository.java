package com.sdat_s4_sprint_backend.repos;

import com.sdat_s4_sprint_backend.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findByPortId(String portId);
    boolean existsByPortId(String portId);
}

