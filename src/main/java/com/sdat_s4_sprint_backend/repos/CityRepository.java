package com.sdat_s4_sprint_backend.repos;

import com.sdat_s4_sprint_backend.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 Gives us full CRUD access from .findAll(), .findById(), .save(), .deleteById()
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    // leave blank for now
}
