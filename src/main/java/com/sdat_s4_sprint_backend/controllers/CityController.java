package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.entity.City;
import com.sdat_s4_sprint_backend.service.CityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/cities") // final path: /api/cities if server.servlet.context-path=/api
public class CityController {

    private final CityService cityServ;

    public CityController(CityService cityServ) {
        this.cityServ = cityServ;
    }

    @GetMapping
    public List<City> getAllCities() {
        return cityServ.getAllCities();
    }

    @GetMapping("/{id}")
    public City getCity(@PathVariable Long id) {
        City c = cityServ.getCity(id);
        if (c == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found");
        return c;
    }

    @PostMapping
    public ResponseEntity<City> addCity(@RequestBody City city, UriComponentsBuilder uri) {
        City saved = cityServ.addCity(city);
        URI location = uri.path("/api/cities/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityServ.deleteCity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/airports")
    public Object getAirportsForCity(@PathVariable Long id) {
        City city = cityServ.getCity(id);
        if (city == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found");
        return city.getAirports();
    }

    @PutMapping("/{id}")
    public City updateCity(@PathVariable Long id, @RequestBody City c) {
        return cityServ.updateCity(id, c);
    }

    @PatchMapping("/{id}")
    public City patchCity(@PathVariable Long id, @RequestBody City partial) {
        return cityServ.patchCity(id, partial);
    }
}
