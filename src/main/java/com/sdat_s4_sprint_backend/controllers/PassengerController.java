package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.entity.Aircraft;
import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.entity.Passenger;
import com.sdat_s4_sprint_backend.service.AirportService;
import com.sdat_s4_sprint_backend.service.PassengerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/passengers")
public class PassengerController {

    private final PassengerService passengerService;
    private final AirportService airportService;

    public PassengerController(PassengerService passengerService, AirportService airportService) {
        this.passengerService = passengerService;
        this.airportService = airportService;
    }

    @GetMapping
    public List<Passenger> getAllPassengers() {
        return passengerService.getAllPassengers();
    }

    @GetMapping("/{id}")
    public Passenger getPassenger(@PathVariable Long id) {
        Passenger p = passengerService.getPassenger(id);
        if (p == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");
        return p;
    }

    // POST /api/passengers?cityId=1
    @PostMapping
    public ResponseEntity<Passenger> addPassenger(@RequestBody Passenger passenger,
                                                  @RequestParam Long cityId,
                                                  UriComponentsBuilder uri) {
        Passenger saved = passengerService.addPassenger(passenger, cityId);
        URI location = uri.path("/api/passengers/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Passenger updatePassenger(@PathVariable Long id, @RequestBody Passenger p) {
        return passengerService.updatePassenger(id, p);
    }

    @PatchMapping("/{id}")
    public Passenger patchPassenger(@PathVariable Long id, @RequestBody Passenger p) {
        return passengerService.patchPassenger(id, p);
    }

    @GetMapping("/{id}/aircraft")
    public Set<Aircraft> getAircraftFlown(@PathVariable Long id) {
        Passenger p = passengerService.getPassenger(id);
        if (p == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");
        return p.getAircraftSet();
    }

    @PutMapping("/{pId}/aircraft/{aId}")
    public ResponseEntity<Void> assignAircraft(@PathVariable Long pId, @PathVariable Long aId) {
        passengerService.assignAircraftToPassenger(pId, aId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/airports")
    public Set<Airport> getAirportForPassenger(@PathVariable Long id) {
        Passenger p = passengerService.getPassenger(id);
        if (p == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");
        return p.getAirports();
    }

    @PatchMapping("/{passengerId}/airports/{airportId}")
    public ResponseEntity<Void> addAirportToPassenger(@PathVariable Long passengerId,
                                                      @PathVariable Long airportId) {
        Passenger passenger = passengerService.getPassenger(passengerId);
        if (passenger == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");

        Airport airport = airportService.getAirport(airportId);
        if (airport == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Airport not found");

        passenger.getAirports().add(airport);
        passengerService.savePassenger(passenger);
        return ResponseEntity.noContent().build();
    }
}
