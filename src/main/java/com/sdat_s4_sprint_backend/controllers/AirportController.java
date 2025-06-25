package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.service.AirportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/airports")
public class AirportController {
    @Autowired
    private AirportService apServ;

    @GetMapping
    public List<Airport> getAllAirports() {
        return apServ.getAllAirports();
    }

    @GetMapping("/{id}")
    public Airport getAirport(@PathVariable Long id) {
        return apServ.getAirport(id);
    }

    @PostMapping
    public Airport addAirport(@RequestBody Airport airport, @RequestParam Long cityId) {
        return apServ.addAirport(airport, cityId); // lets you POST /airports?cityId=1
    }

    @DeleteMapping("/{id}")
    public void deleteAirport(@PathVariable Long id) {
        apServ.deleteAirport(id);
    }

    @PutMapping("/{id}")
    public Airport updateAirport(@PathVariable Long id, @RequestBody Airport a) {
        return apServ.updateAirport(id, a);
    }

    @PatchMapping("/{id}")
    public Airport patchAirport(@PathVariable Long id, @RequestBody Airport p) {
        return apServ.patchAirport(id, p);
    }

}
