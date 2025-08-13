package com.sdat_s4_sprint_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String airline;          // e.g., "AC"
    private String flightNumber;     // e.g., "AC123"

    @ManyToOne(optional = false)
    @JoinColumn(name = "departure_airport_id")
    private Airport departureAirport;

    @ManyToOne(optional = false)
    @JoinColumn(name = "arrival_airport_id")
    private Airport arrivalAirport;

    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;

    @Enumerated(EnumType.STRING)
    private FlightStatus status;     // SCHEDULED / ...

    private Integer distanceKm;      // optional
    private Integer durationMinutes; // optional

    // ---- getters/setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public Airport getDepartureAirport() { return departureAirport; }
    public void setDepartureAirport(Airport departureAirport) { this.departureAirport = departureAirport; }

    public Airport getArrivalAirport() { return arrivalAirport; }
    public void setArrivalAirport(Airport arrivalAirport) { this.arrivalAirport = arrivalAirport; }

    public LocalDateTime getScheduledDeparture() { return scheduledDeparture; }
    public void setScheduledDeparture(LocalDateTime scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }

    public LocalDateTime getScheduledArrival() { return scheduledArrival; }
    public void setScheduledArrival(LocalDateTime scheduledArrival) { this.scheduledArrival = scheduledArrival; }

    public FlightStatus getStatus() { return status; }
    public void setStatus(FlightStatus status) { this.status = status; }

    public Integer getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Integer distanceKm) { this.distanceKm = distanceKm; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}
