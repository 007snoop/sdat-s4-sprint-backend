package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.entity.City;
import com.sdat_s4_sprint_backend.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cities")
public class CityController {
    @Autowired
    private CityService cityServ;

    @GetMapping
    public List<City> getAllCities() {
        return cityServ.getAllCities();
    }

    @GetMapping("/{id}")
    public City getCity(@PathVariable Long id) {
        return cityServ.getCity(id);
    }

    @PostMapping
    public City addCity(@RequestBody City city) {
        return cityServ.addCity(city);
    }

    @DeleteMapping("/{id}")
    public void deleteCity(@PathVariable Long id) {
        cityServ.deleteCity(id);
    }

    @GetMapping("/{id}/airports")
    public List<?> getAirportsForCity(@PathVariable Long id) {
        City city = cityServ.getCity(id);
        return city != null ? city.getAirports() : List.of();
    }

    @PutMapping("/{id}")
    public City updateCity(@PathVariable Long id, @RequestBody City c) {
        return cityServ.updateCity(id, c);
    }
}
