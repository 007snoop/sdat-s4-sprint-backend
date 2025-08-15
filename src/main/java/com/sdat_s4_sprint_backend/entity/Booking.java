package com.sdat_s4_sprint_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "bookings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "passenger_id"}),
        indexes = {
                @Index(name = "idx_bookings_flight", columnList = "flight_id"),
                @Index(name = "idx_bookings_passenger", columnList = "passenger_id")
        }
)
public class Booking {

    public enum BookingStatus { RESERVED, CONFIRMED, CHECKED_IN, BOARDED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    @JsonIgnore
    private Flight flight;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    @JsonIgnore
    private Passenger passenger;

    @Column(nullable = false, unique = true, length = 64)
    private String bookingRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingStatus status = BookingStatus.RESERVED;

    @Column(length = 8)
    private String seatNumber;

    @Column(length = 16)
    private String fareClass;

    private OffsetDateTime bookedAt;

    @PrePersist
    void prePersist() {
        if (bookedAt == null) bookedAt = OffsetDateTime.now();
    }

    // --- getters / setters ---
    public Long getId() { return id; }

    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }

    public Passenger getPassenger() { return passenger; }
    public void setPassenger(Passenger passenger) { this.passenger = passenger; }

    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getFareClass() { return fareClass; }
    public void setFareClass(String fareClass) { this.fareClass = fareClass; }

    public OffsetDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(OffsetDateTime bookedAt) { this.bookedAt = bookedAt; }

    // Convenience accessors for UI (derived from Flight)
    @Transient public Airport getDepartureAirport() {
        return (flight != null) ? flight.getDepartureAirport() : null;
    }
    @Transient public Airport getArrivalAirport() {
        return (flight != null) ? flight.getArrivalAirport() : null;
    }
}
