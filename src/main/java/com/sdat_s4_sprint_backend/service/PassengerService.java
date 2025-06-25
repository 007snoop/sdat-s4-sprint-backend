package com.sdat_s4_sprint_backend.service;

import com.sdat_s4_sprint_backend.entity.City;
import com.sdat_s4_sprint_backend.entity.Passenger;
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
}
