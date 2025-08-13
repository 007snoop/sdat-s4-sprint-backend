package com.sdat_s4_sprint_backend.repos;

import com.sdat_s4_sprint_backend.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, Long> { }
