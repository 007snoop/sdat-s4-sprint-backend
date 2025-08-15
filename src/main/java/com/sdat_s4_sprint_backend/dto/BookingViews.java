package com.sdat_s4_sprint_backend.dto;

import java.time.LocalDateTime;

public class BookingViews {

    // For "who's on this flight?"
    public record FlightManifestRow(
            Long bookingId,
            String bookingRef,
            String seatNumber,
            String fareClass,
            Long passengerId,
            String firstName,
            String lastName,
            LocalDateTime scheduledDeparture,
            LocalDateTime scheduledArrival,
            String departurePortId,
            String departureAirportName,
            String arrivalPortId,
            String arrivalAirportName
    ) {}

    // For "what trips does this passenger have?"
    public record PassengerItineraryRow(
            Long bookingId,
            String bookingRef,
            String seatNumber,
            String fareClass,
            Long flightId,
            String airline,
            String flightNumber,
            String departurePortId,
            String departureAirportName,
            String arrivalPortId,
            String arrivalAirportName,
            LocalDateTime scheduledDeparture,
            LocalDateTime scheduledArrival,
            String status
    ) {}
}
