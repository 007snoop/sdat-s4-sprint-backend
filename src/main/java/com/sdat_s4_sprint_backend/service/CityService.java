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
}
