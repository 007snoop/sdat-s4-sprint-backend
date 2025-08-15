package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.entity.Flight;
import com.sdat_s4_sprint_backend.repos.FlightRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flights")
@CrossOrigin
public class FlightController {

    private final FlightRepository flightRepo;

    public FlightController(FlightRepository flightRepo) {
        this.flightRepo = flightRepo;
    }

    // Simple list (cap to 200 to prevent accidental huge responses)
    @GetMapping
    public List<Flight> getAll(@RequestParam(defaultValue = "200") int limit) {
        int safe = Math.max(1, Math.min(2000, limit));
        return flightRepo.findAll(PageRequest.of(0, safe)).getContent();
    }

    // Optional: paginated
    @GetMapping("/page")
    public Page<Flight> page(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "50") int size) {
        int s = Math.max(1, Math.min(500, size));
        return flightRepo.findAll(PageRequest.of(page, s));
    }

    // Optional: count
    @GetMapping("/count")
    public long count() {
        return flightRepo.count();
    }

    // === New endpoints to get airports linked to a flight ===

    @GetMapping("/{id}/departure-airport")
    public ResponseEntity<Airport> getDepartureAirport(@PathVariable Long id) {
        return flightRepo.findById(id)
                .map(f -> ResponseEntity.ok(f.getDepartureAirport()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/arrival-airport")
    public ResponseEntity<Airport> getArrivalAirport(@PathVariable Long id) {
        return flightRepo.findById(id)
                .map(f -> ResponseEntity.ok(f.getArrivalAirport()))
                .orElse(ResponseEntity.notFound().build());
    }
}
