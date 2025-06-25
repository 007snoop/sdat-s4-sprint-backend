package com.sdat_s4_sprint_backend.service;

import com.sdat_s4_sprint_backend.entity.Aircraft;
import com.sdat_s4_sprint_backend.repos.AircraftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AircraftService {
    @Autowired
    private AircraftRepository aircraftRepository;

    public List<Aircraft> getAllAircraft() {
        return aircraftRepository.findAll();
    }
    public Aircraft getAircraft(Long id) {
        return aircraftRepository.findById(id).orElse(null);
    }
    public Aircraft addAircraft(Aircraft a) {
        return aircraftRepository.save(a);
    }
    public void deleteAircraft(Long id) {
        aircraftRepository.deleteById(id);
    }
}
