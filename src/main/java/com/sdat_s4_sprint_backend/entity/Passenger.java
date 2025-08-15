package com.sdat_s4_sprint_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName, lastName, phoneNumber;

    @ManyToOne
    @JoinColumn(name = "city_id")
    @JsonIgnore
    private City city;

    @ManyToMany
    @JoinTable(
            name = "passenger_aircraft",
            joinColumns = @JoinColumn(name = "passenger_id"),
            inverseJoinColumns = @JoinColumn(name = "aircraft_id")
    )
    @JsonIgnore
    private Set<Aircraft> aircraftSet = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "passenger_airport",
            joinColumns = @JoinColumn(name = "passenger_id"),
            inverseJoinColumns = @JoinColumn(name = "airport_id")
    )
    @JsonIgnore
    private Set<Airport> airports = new HashSet<>();

    // NEW: link Passenger -> Booking (one passenger can have many bookings)
    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // avoid recursion when serializing Passenger
    private List<Booking> bookings = new ArrayList<>();

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Set<Aircraft> getAircraftSet() { return aircraftSet; }
    public void setAircraftSet(Set<Aircraft> aircraft) { this.aircraftSet = aircraft; }

    public Set<Airport> getAirports() { return airports; }
    public void setAirports(Set<Airport> airports) { this.airports = airports; }

    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}