package com.sdat_s4_sprint_backend.service;

import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.repos.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AirportService {

    @Autowired
    private AirportRepository AirportRepo;

    public List<Airport> getAllAirports() {
        return AirportRepo.findAll();
    }

    public Airport getAirport(Long id) {
        return AirportRepo.findById(id).orElse(null);
    }

    public Airport addAirport(Airport ap) {
        return AirportRepo.save(ap);
    }

    public void deleteAirport(Long id) {
        AirportRepo.deleteById(id);
    }
}
