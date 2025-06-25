package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.entity.Passenger;
import com.sdat_s4_sprint_backend.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/passengers")
public class PassengerController {
    @Autowired
    private PassengerService passengerService;

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
}
