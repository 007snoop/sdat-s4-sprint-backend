package com.sdat_s4_sprint_backend.service;

import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.entity.City;
import com.sdat_s4_sprint_backend.repos.AirportRepository;
import com.sdat_s4_sprint_backend.repos.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class AirportService {

    @Autowired
    private CityRepository cityRepo;

    @Autowired
    private AirportRepository AirportRepo;

    public List<Airport> getAllAirports() {
        return AirportRepo.findAll();
    }

    public Airport getAirport(Long id) {
        return AirportRepo.findById(id).orElse(null);
    }

    public Airport addAirport(Airport ap, Long cityId) {
        City city = cityRepo.findById(cityId).orElse(null);
        if (city != null) {
            ap.setCity(city);
            return AirportRepo.save(ap);
        } else {
            return null;
        }
    }

    public void deleteAirport(Long id) {
        AirportRepo.deleteById(id);
    }

    public Airport updateAirport(Long id, Airport a) {
        Airport e = AirportRepo.findById(id).orElse(null);
        if (e != null) {
            e.setName(a.getName());
            e.setPortId(a.getPortId());
            return AirportRepo.save(e);
        } return null;
    }

    public Airport patchAirport(Long id, Airport p) {
        Airport e = AirportRepo.findById(id).orElse(null);
        if (e != null) {
            if (p.getName() != null) e.setName(p.getName());
            if (p.getCity() != null) e.setCity(p.getCity());
            if (p.getPortId() != null) e.setPortId(p.getPortId());
            return AirportRepo.save(e);
        } return null;
    }
}
