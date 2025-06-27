package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.entity.Aircraft;
import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.entity.Passenger;
import com.sdat_s4_sprint_backend.service.AirportService;
import com.sdat_s4_sprint_backend.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/passengers")
public class PassengerController {
    @Autowired
    private PassengerService passengerService;
    @Autowired
    private AirportService airportService;

    @GetMapping
    public List<Passenger> getAllPassengers() {
        return passengerService.getAllPassengers();
    }
    @GetMapping("/{id}")
    public Passenger getPassenger(@PathVariable Long id) {
        return passengerService.getPassenger(id);
    }
    @PostMapping
    public Passenger addPassenger(@RequestBody Passenger passenger, @RequestParam Long cityId) {
        return passengerService.addPassenger(passenger, cityId);
    }
    @DeleteMapping("/{id}")
    public void deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
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

        if (p != null) {
            return p.getAircraftSet();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");
        }
    }

    @PutMapping("/{pId}/aircraft/{aId}")
    public Passenger assignAircraft(@PathVariable Long pId, @PathVariable Long aId) {
        return passengerService.assignAircraftToPassenger(pId,aId);
    }

    @GetMapping("/{id}/airports")
    public Set<Airport> getAirportForPassenger(@PathVariable Long id) {
        Passenger p = passengerService.getPassenger(id);
        if (p != null) {
            return p.getAirports();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found");
        }
    }

    @PatchMapping("/{passengerId}/airports/{airportId}")
    public ResponseEntity<Void> addAirportToPassenger(
            @PathVariable Long passengerId,
            @PathVariable Long airportId) {

        Passenger passenger = passengerService.getPassenger(passengerId);
        Airport airport = airportService.getAirport(airportId); // You'll need this too

        if (passenger != null && airport != null) {
            passenger.getAirports().add(airport);
            passengerService.savePassenger(passenger);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
