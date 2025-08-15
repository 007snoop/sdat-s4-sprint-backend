package com.sdat_s4_sprint_backend.service;

import com.sdat_s4_sprint_backend.entity.Booking;
import com.sdat_s4_sprint_backend.entity.Flight;
import com.sdat_s4_sprint_backend.entity.Passenger;
import com.sdat_s4_sprint_backend.repos.BookingRepository;
import com.sdat_s4_sprint_backend.repos.FlightRepository;
import com.sdat_s4_sprint_backend.repos.PassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookingRepo;
    private final FlightRepository flightRepo;
    private final PassengerRepository passengerRepo;

    public BookingService(BookingRepository b, FlightRepository f, PassengerRepository p) {
        this.bookingRepo = b; this.flightRepo = f; this.passengerRepo = p;
    }

    @Transactional
    public Booking create(Long flightId, Long passengerId, String seatNumber, String fareClass) {
        Flight flight = flightRepo.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        Passenger pax = passengerRepo.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found"));

        Booking b = new Booking();
        b.setFlight(flight);
        b.setPassenger(pax);
        b.setSeatNumber(seatNumber);
        b.setFareClass(fareClass);
        b.setStatus(Booking.BookingStatus.CONFIRMED);
        b.setBookingRef("BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        return bookingRepo.save(b);
    }

    @Transactional public Booking updateStatus(Long id, Booking.BookingStatus status) {
        Booking b = bookingRepo.findById(id).orElseThrow();
        b.setStatus(status);
        return b;
    }

    @Transactional public void cancel(Long id) {
        Booking b = bookingRepo.findById(id).orElseThrow();
        b.setStatus(Booking.BookingStatus.CANCELLED);
    }
}
