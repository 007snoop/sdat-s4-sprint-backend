package com.sdat_s4_sprint_backend.service;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.sdat_s4_sprint_backend.entity.Aircraft;
import com.sdat_s4_sprint_backend.entity.City;
import com.sdat_s4_sprint_backend.entity.Passenger;
import com.sdat_s4_sprint_backend.repos.AircraftRepository;
import com.sdat_s4_sprint_backend.repos.CityRepository;
import com.sdat_s4_sprint_backend.repos.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {
    @Autowired
    private PassengerRepository passengerRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private AircraftRepository aircraftRepository;


    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }
    public Passenger getPassenger(Long id) {
        return passengerRepository.findById(id).orElse(null);
    }
    public Passenger addPassenger(Passenger p, Long cityId) {
        cityRepository.findById(cityId).ifPresent(p::setCity);
        return passengerRepository.save(p);
    }
    public void deletePassenger(Long id) {
        passengerRepository.deleteById(id);
    }

    public Passenger updatePassenger(Long id, Passenger p) {
        Passenger e = passengerRepository.findById(id).orElse(null);
        if (e != null) {
            e.setFirstName(p.getFirstName());
            e.setLastName(p.getLastName());
            e.setPhoneNumber(p.getPhoneNumber());
            return passengerRepository.save(e);
        } return null;
    }

    public Passenger patchPassenger(Long id, Passenger p) {
        Passenger e = passengerRepository.findById(id).orElse(null);
        if (e != null) {
            if (p.getFirstName() != null) e.setFirstName(p.getFirstName());
            if (p.getLastName() != null) e.setLastName(p.getLastName());
            if (p.getPhoneNumber() != null) e.setPhoneNumber(p.getPhoneNumber());
            return passengerRepository.save(e);
        } return null;
    }

    public Passenger assignAircraftToPassenger(Long pId, Long aId) {
        Passenger p = passengerRepository.findById(pId).orElseThrow(() -> new RuntimeException("Passenger not found"));
        Aircraft a = aircraftRepository.findById(aId).orElseThrow(() -> new RuntimeException("Aircraft not found"));

        p.getAircraftSet().add(a);
        return passengerRepository.save(p);

    }

    public Passenger savePassenger(Passenger passenger) {
        return passengerRepository.save(passenger);
    }
}
