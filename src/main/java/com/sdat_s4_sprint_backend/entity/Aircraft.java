package com.sdat_s4_sprint_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String airlineName;
    private int numOfPassengers;

    // EAGER so /aircraft/{id}/passengers doesn't hit LazyInitializationException
    @ManyToMany(mappedBy = "aircraftSet", fetch = FetchType.EAGER)
    @JsonIgnore // only ignored when serializing Aircraft itself (not when returning the Set directly)
    private Set<Passenger> passengerSet = new HashSet<>();

    // EAGER so /aircraft/{id}/airports doesn't hit LazyInitializationException
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "aircraft_airport",
            joinColumns = @JoinColumn(name = "aircraft_id"),
            inverseJoinColumns = @JoinColumn(name = "airport_id")
    )
    @JsonIgnore
    private Set<Airport> airportSet = new HashSet<>();

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getAirlineName() {
        return airlineName;
    }
    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public int getNumOfPassengers() {
        return numOfPassengers;
    }
    public void setNumOfPassengers(int numOfPassengers) {
        this.numOfPassengers = numOfPassengers;
    }

    public Set<Passenger> getPassengers() {
        return passengerSet;
    }
    public void setPassengers(Set<Passenger> passengers) {
        this.passengerSet = passengers;
    }

    public Set<Airport> getAirports() {
        return airportSet;
    }
    public void setAirports(Set<Airport> airports) {
        this.airportSet = airports;
    }
}
