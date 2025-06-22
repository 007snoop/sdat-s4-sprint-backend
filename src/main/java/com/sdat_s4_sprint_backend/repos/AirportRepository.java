package com.sdat_s4_sprint_backend.repos;

import com.sdat_s4_sprint_backend.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, Long> {
}
