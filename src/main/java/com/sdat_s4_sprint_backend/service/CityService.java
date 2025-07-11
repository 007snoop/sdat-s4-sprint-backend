package com.sdat_s4_sprint_backend.service;

import com.sdat_s4_sprint_backend.entity.City;
import com.sdat_s4_sprint_backend.repos.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {
    @Autowired
    private CityRepository cityRepo;

    public List<City> getAllCities() {
        return cityRepo.findAll();
    }

    public City getCity(Long id) {
        return cityRepo.findById(id).orElse(null);
    }

    public City addCity(City city) {
        return cityRepo.save(city);
    }

    public void deleteCity(Long id) {
        cityRepo.deleteById(id);
    }

    public City updateCity(Long id, City c) {
        City e = cityRepo.findById(id).orElse(null);
        if (e != null) {
            e.setName(c.getName());
            e.setProvince(c.getProvince());
            e.setAirports(c.getAirports());
            e.setPopulation(c.getPopulation());
            return cityRepo.save(e);
        } return null;
    }

    public City patchCity(Long id, City p) {
        City e = cityRepo.findById(id).orElse(null);
        if (e != null) {
            if (p.getName() != null) e.setName(p.getName());
            if (p.getProvince() != null) e.setProvince(p.getProvince());
            if (p.getPopulation() != 0) e.setPopulation(p.getPopulation());
            if (p.getAirports() != null) e.setAirports(p.getAirports());
            return cityRepo.save(e);
        } return null;
    }
}
