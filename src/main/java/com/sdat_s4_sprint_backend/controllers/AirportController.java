package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.service.AirportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/airports") // final path: /api/airports if server.servlet.context-path=/api
public class AirportController {

    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @GetMapping
    public List<Airport> getAllAirports() {
        return airportService.getAllAirports();
    }

    @GetMapping("/{id}")
    public Airport getAirport(@PathVariable Long id) {
        Airport a = airportService.getAirport(id);
        if (a == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Airport not found");
        return a;
    }

    // POST /api/airports?cityId=1
    @PostMapping
    public ResponseEntity<Airport> addAirport(@RequestBody Airport airport,
                                              @RequestParam Long cityId,
                                              UriComponentsBuilder uri) {
        Airport saved = airportService.addAirport(airport, cityId);
        URI location = uri.path("/api/airports/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAirport(@PathVariable Long id) {
        airportService.deleteAirport(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Airport updateAirport(@PathVariable Long id, @RequestBody Airport a) {
        return airportService.updateAirport(id, a);
    }

    @PatchMapping("/{id}")
    public Airport patchAirport(@PathVariable Long id, @RequestBody Airport partial) {
        return airportService.patchAirport(id, partial);
    }

    // If we later add a finder by code in the service, we can expose it like this:
    // @GetMapping("/code/{code}")
    // public Airport getByCode(@PathVariable String code) {
    //     Airport a = airportService.getByCode(code);
    //     if (a == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Airport not found");
    //     return a;
    // }
}
