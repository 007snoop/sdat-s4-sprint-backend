package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.entity.Aircraft;
import com.sdat_s4_sprint_backend.service.AircraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aircraft")
public class AircraftController {
    @Autowired
    private AircraftService aircraftService;

    @GetMapping
    public List<Aircraft> getAllAircraft() {
        return aircraftService.getAllAircraft();
    }

    @GetMapping("/{id}")
    public Aircraft getAircraft(@PathVariable Long id) {
        return aircraftService.getAircraft(id);
    }

    @PostMapping
    public Aircraft addAircraft(@RequestBody Aircraft a) {
        return aircraftService.addAircraft(a);
    }

    @DeleteMapping("/{id}")
    public void deleteAircraft(@PathVariable Long id) {
        aircraftService.deleteAircraft(id);
    }

    @PutMapping("/{id}")
    public Aircraft updateAircraft(@PathVariable Long id, @RequestBody Aircraft a) {
        return aircraftService.updateAircraft(id, a);
    }

    @PatchMapping("/{id}")
    public Aircraft patchAircraft(@PathVariable Long id, @RequestBody Aircraft p) {
        return aircraftService.patchAircraft(id, p);
    }

    @PutMapping("/{aId}/airports/apId")
    public void addAptoAc(@PathVariable Long acId, @PathVariable Long apId) {
        aircraftService.addAirportToAircraft(acId,apId);
    }
}
