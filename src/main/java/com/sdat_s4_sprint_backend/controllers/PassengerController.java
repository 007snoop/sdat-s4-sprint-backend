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
@RequestMapping({"/passengers", "/api/passengers"}) // support both base paths
public class PassengerController {

    private final PassengerService passengerService;
    private final AirportService airportService;

    public PassengerController(PassengerService passengerService, AirportService airportService) {
        this.passengerService = passengerService;
        this.airportService = airportService;
    }

    /* --------------------------- CRUD --------------------------- */

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

    /**
     * Create a passenger.
     * Accepts either:
     *   - query param ?cityId=123
     *   - or JSON body with {"city":{"id":123}} (if your entity allows it)
     */
    @PostMapping
    public ResponseEntity<Passenger> addPassenger(@RequestBody Passenger passenger,
                                                  @RequestParam(name = "cityId", required = false) Long cityId,
                                                  UriComponentsBuilder uri) {
        // If city wasn't passed as query param, try to read from JSON body (if available)
        if (cityId == null && passenger.getCity() != null) {
            cityId = passenger.getCity().getId();
        }

        Passenger saved = passengerService.addPassenger(passenger, cityId);
        URI location = uri.path("/passengers/{id}").buildAndExpand(saved.getId()).toUri();
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

    /* ------------------------ Relationships ------------------------ */

    // Aircraft flown by passenger
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

    // Airports associated with passenger
    @GetMapping("/{id}/airports")
    public Set<Airport> getAirportForPassenger(@PathVariable Long id) {
        Passenger p = passengerService.getPassenger(id);
        if (p == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");
        return p.getAirports();
    }

    // Idempotent add
    @PutMapping("/{passengerId}/airports/{airportId}")
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

    // Optional: remove mapping
    @DeleteMapping("/{passengerId}/airports/{airportId}")
    public ResponseEntity<Void> removeAirportFromPassenger(@PathVariable Long passengerId,
                                                           @PathVariable Long airportId) {
        Passenger passenger = passengerService.getPassenger(passengerId);
        if (passenger == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");

        Airport airport = airportService.getAirport(airportId);
        if (airport == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Airport not found");

        passenger.getAirports().remove(airport);
        passengerService.savePassenger(passenger);
        return ResponseEntity.noContent().build();
    }
}
