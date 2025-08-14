package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.entity.Aircraft;
import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.entity.Passenger;
import com.sdat_s4_sprint_backend.service.AircraftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/aircraft") // final path becomes /api/aircraft if you set server.servlet.context-path=/api
public class AircraftController {

    private final AircraftService aircraftService;

    public AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @GetMapping
    public List<Aircraft> getAllAircraft() {
        return aircraftService.getAllAircraft();
    }

    @GetMapping("/{id}")
    public Aircraft getAircraft(@PathVariable Long id) {
        Aircraft a = aircraftService.getAircraft(id);
        if (a == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aircraft not found");
        return a;
    }

    @PostMapping
    public ResponseEntity<Aircraft> addAircraft(@RequestBody Aircraft a, UriComponentsBuilder uri) {
        Aircraft saved = aircraftService.addAircraft(a);
        URI location = uri.path("/api/aircraft/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraft(@PathVariable Long id) {
        aircraftService.deleteAircraft(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Aircraft updateAircraft(@PathVariable Long id, @RequestBody Aircraft a) {
        return aircraftService.updateAircraft(id, a);
    }

    @PatchMapping("/{id}")
    public Aircraft patchAircraft(@PathVariable Long id, @RequestBody Aircraft p) {
        return aircraftService.patchAircraft(id, p);
    }

    // Idempotent relation add: PUT /api/aircraft/{acId}/airports/{apId}
    @PutMapping("/{acId}/airports/{apId}")
    public ResponseEntity<Void> addAptoAc(@PathVariable Long acId, @PathVariable Long apId) {
        aircraftService.addAirportToAircraft(acId, apId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/passengers")
    public Set<Passenger> getPassengersOnAircraft(@PathVariable Long id) {
        Aircraft a = aircraftService.getAircraft(id);
        if (a == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aircraft not found");
        return a.getPassengers();
    }

    @GetMapping("/{id}/airports")
    public Set<Airport> getAirportsForAircraft(@PathVariable Long id) {
        Aircraft a = aircraftService.getAircraft(id);
        if (a == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aircraft not found");
        return a.getAirports();
    }
}
