package com.sdat_s4_sprint_backend.controllers;

import com.sdat_s4_sprint_backend.dto.BookingViews.FlightManifestRow;
import com.sdat_s4_sprint_backend.dto.BookingViews.PassengerItineraryRow;
import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.entity.Booking;
import com.sdat_s4_sprint_backend.entity.Flight;
import com.sdat_s4_sprint_backend.entity.Passenger;
import com.sdat_s4_sprint_backend.repos.BookingRepository;
import com.sdat_s4_sprint_backend.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@CrossOrigin
public class BookingController {

    public record CreateBookingRequest(Long flightId, Long passengerId, String seatNumber, String fareClass) {}
    public record UpdateStatusRequest(Booking.BookingStatus status) {}

    private final BookingService bookingService;
    private final BookingRepository bookingRepo;

    public BookingController(BookingService bookingService, BookingRepository bookingRepo) {
        this.bookingService = bookingService; this.bookingRepo = bookingRepo;
    }

    @PostMapping
    public ResponseEntity<Booking> create(@RequestBody CreateBookingRequest req) {
        return ResponseEntity.ok(
                bookingService.create(req.flightId(), req.passengerId(), req.seatNumber(), req.fareClass())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> get(@PathVariable Long id) {
        return bookingRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-passenger/{passengerId}")
    public List<Booking> byPassenger(@PathVariable Long passengerId) {
        return bookingRepo.findByPassenger_Id(passengerId);
    }

    @GetMapping("/by-flight/{flightId}")
    public List<Booking> byFlight(@PathVariable Long flightId) {
        return bookingRepo.findByFlight_Id(flightId);
    }

    @PatchMapping("/{id}/status")
    public Booking updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest req) {
        return bookingService.updateStatus(id, req.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        bookingService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    // ===== DTO endpoints =====

    // Passenger list for a flight (manifest) with times + airport info
    @GetMapping("/by-flight/{flightId}/manifest")
    public List<FlightManifestRow> flightManifest(@PathVariable Long flightId) {
        return bookingRepo.findByFlight_Id(flightId).stream().map(b -> {
            Passenger p = b.getPassenger();
            Flight f = b.getFlight();
            Airport dep = (f != null) ? f.getDepartureAirport() : null;
            Airport arr = (f != null) ? f.getArrivalAirport() : null;

            return new FlightManifestRow(
                    b.getId(),
                    b.getBookingRef(),
                    b.getSeatNumber(),
                    b.getFareClass(),
                    (p != null) ? p.getId() : null,
                    (p != null) ? p.getFirstName() : null,
                    (p != null) ? p.getLastName() : null,
                    (f != null) ? f.getScheduledDeparture() : null,
                    (f != null) ? f.getScheduledArrival() : null,
                    (dep != null) ? dep.getPortId() : null,
                    (dep != null) ? dep.getName() : null,
                    (arr != null) ? arr.getPortId() : null,
                    (arr != null) ? arr.getName() : null
            );
        }).toList();
    }

    // Passenger itinerary (where they’re going) with times + airport info
    @GetMapping("/by-passenger/{passengerId}/itinerary")
    public List<PassengerItineraryRow> passengerItinerary(@PathVariable Long passengerId) {
        return bookingRepo.findByPassenger_Id(passengerId).stream().map(b -> {
            Flight f = b.getFlight();
            Airport dep = (f != null) ? f.getDepartureAirport() : null;
            Airport arr = (f != null) ? f.getArrivalAirport() : null;

            return new PassengerItineraryRow(
                    b.getId(),
                    b.getBookingRef(),
                    b.getSeatNumber(),
                    b.getFareClass(),
                    (f != null) ? f.getId() : null,
                    (f != null) ? f.getAirline() : null,
                    (f != null) ? f.getFlightNumber() : null,
                    (dep != null) ? dep.getPortId() : null,
                    (dep != null) ? dep.getName() : null,
                    (arr != null) ? arr.getPortId() : null,
                    (arr != null) ? arr.getName() : null,
                    (f != null) ? f.getScheduledDeparture() : null,
                    (f != null) ? f.getScheduledArrival() : null,
                    (b.getStatus() != null) ? b.getStatus().name() : null
            );
        }).toList();
    }
}
