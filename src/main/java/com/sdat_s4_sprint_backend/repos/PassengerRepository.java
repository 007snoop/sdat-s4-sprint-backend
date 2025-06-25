package com.sdat_s4_sprint_backend.repos;

import com.sdat_s4_sprint_backend.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
}
