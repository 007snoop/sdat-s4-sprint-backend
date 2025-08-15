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
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/aircraft", "/api/aircraft"})
public class AircraftController {

    private final AircraftService aircraftService;

    public AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    // Minimal DTO to avoid JSON recursion/lazy issues
    public static class AircraftDto {
        public Long id;
        public String type;
        public String airlineName;
        public int numOfPassengers;

        public static AircraftDto from(Aircraft a) {
            AircraftDto d = new AircraftDto();
            d.id = a.getId();
            d.type = a.getType();
            d.airlineName = a.getAirlineName();
            d.numOfPassengers = a.getNumOfPassengers();
            return d;
        }
    }

    @GetMapping
    public List<AircraftDto> getAllAircraft() {
        return aircraftService.getAllAircraft().stream()
                .map(AircraftDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AircraftDto getAircraft(@PathVariable Long id) {
        Aircraft a = aircraftService.getAircraft(id);
        if (a == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aircraft not found");
        return AircraftDto.from(a);
    }

    @GetMapping("/count")
    public long count() {
        return aircraftService.getAllAircraft().size();
    }

    @PostMapping
    public ResponseEntity<AircraftDto> addAircraft(@RequestBody Aircraft a, UriComponentsBuilder uri) {
        Aircraft saved = aircraftService.addAircraft(a);
        URI location = uri.path("/aircraft/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(AircraftDto.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraft(@PathVariable Long id) {
        aircraftService.deleteAircraft(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public AircraftDto updateAircraft(@PathVariable Long id, @RequestBody Aircraft a) {
        return AircraftDto.from(aircraftService.updateAircraft(id, a));
    }

    @PatchMapping("/{id}")
    public AircraftDto patchAircraft(@PathVariable Long id, @RequestBody Aircraft p) {
        return AircraftDto.from(aircraftService.patchAircraft(id, p));
    }

    // Relations (kept as-is)
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