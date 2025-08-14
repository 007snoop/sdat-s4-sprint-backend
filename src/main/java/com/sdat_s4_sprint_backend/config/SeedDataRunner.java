package com.sdat_s4_sprint_backend.config;

import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.entity.City;
import com.sdat_s4_sprint_backend.entity.Flight;
import com.sdat_s4_sprint_backend.entity.FlightStatus;
import com.sdat_s4_sprint_backend.repos.AirportRepository;
import com.sdat_s4_sprint_backend.repos.CityRepository;
import com.sdat_s4_sprint_backend.repos.FlightRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Seeds a small dataset for local/demo environments.
 * Controlled by env var APP_SEED_ENABLED (default true).
 * Safe to re-run (won’t duplicate cities/airports; flights seeded only if table empty).
 */
@Configuration
public class SeedDataRunner {

    private boolean seedEnabled() {
        String v = System.getenv("APP_SEED_ENABLED");
        return v == null || Boolean.parseBoolean(v);
    }

    @Bean
    @Transactional
    public ApplicationRunner seed(
            CityRepository cityRepo,
            AirportRepository airportRepo,
            FlightRepository flightRepo
    ) {
        return args -> {
            if (!seedEnabled()) return;

            // ---------- Cities & Airports ----------
            // code (IATA), airport name, city name, country code (stored in City.province for now)
            List<String[]> airportsSeed = List.of(
                    new String[]{"ATL","Hartsfield–Jackson Atlanta","Atlanta","US"},
                    new String[]{"PEK","Beijing Capital","Beijing","CN"},
                    new String[]{"LAX","Los Angeles Intl","Los Angeles","US"},
                    new String[]{"DXB","Dubai Intl","Dubai","AE"},
                    new String[]{"HND","Tokyo Haneda","Tokyo","JP"},
                    new String[]{"ORD","O'Hare Intl","Chicago","US"},
                    new String[]{"LHR","Heathrow","London","GB"},
                    new String[]{"CDG","Charles de Gaulle","Paris","FR"},
                    new String[]{"DFW","Dallas/Fort Worth","Dallas","US"},
                    new String[]{"AMS","Schiphol","Amsterdam","NL"},
                    new String[]{"FRA","Frankfurt","Frankfurt","DE"},
                    new String[]{"IST","Istanbul","Istanbul","TR"},
                    new String[]{"SIN","Changi","Singapore","SG"},
                    new String[]{"ICN","Incheon","Seoul","KR"},
                    new String[]{"SFO","San Francisco Intl","San Francisco","US"},
                    new String[]{"JFK","John F. Kennedy","New York","US"},
                    new String[]{"MEX","Benito Juárez","Mexico City","MX"},
                    new String[]{"GRU","Guarulhos","São Paulo","BR"},
                    new String[]{"BCN","Barcelona","Barcelona","ES"},
                    new String[]{"MAD","Madrid–Barajas","Madrid","ES"}
            );

            Map<String, Airport> byCode = new HashMap<>();

            for (String[] row : airportsSeed) {
                String code = row[0];
                String name = row[1];
                String cityName = row[2];
                String countryCode = row[3];

                // Find or create City (using province to stash country code; population default 0)
                City city = cityRepo.findByNameIgnoreCase(cityName).orElseGet(() -> {
                    City c = new City();
                    c.setName(cityName);
                    c.setProvince(countryCode);
                    c.setPopulation(0);
                    return cityRepo.save(c);
                });

                // Find or create Airport by portId (IATA)
                Airport airport = airportRepo.findByPortId(code).orElseGet(() -> {
                    Airport ap = new Airport();
                    ap.setPortId(code);
                    ap.setName(name);
                    ap.setCity(city);
                    return airportRepo.save(ap);
                });

                // Keep existing airport in sync with seed (idempotent update)
                boolean changed = false;
                if (!Objects.equals(airport.getName(), name)) {
                    airport.setName(name);
                    changed = true;
                }
                if (airport.getPortId() == null || !Objects.equals(airport.getPortId(), code)) {
                    airport.setPortId(code);
                    changed = true;
                }
                if (airport.getCity() == null || !Objects.equals(airport.getCity().getId(), city.getId())) {
                    airport.setCity(city);
                    changed = true;
                }
                if (changed) airportRepo.save(airport);

                byCode.put(code, airport);
            }

            // ---------- Flights ----------
            if (flightRepo.count() == 0) {
                List<Airport> airports = new ArrayList<>(byCode.values());
                if (airports.size() >= 2) {
                    Random rnd = new Random(42);
                    List<Flight> flights = new ArrayList<>(40);

                    for (int i = 0; i < 40; i++) {
                        Airport from = airports.get(rnd.nextInt(airports.size()));
                        Airport to;
                        do {
                            to = airports.get(rnd.nextInt(airports.size()));
                        } while (Objects.equals(from.getId(), to.getId()));

                        LocalDateTime dep = LocalDateTime.now().plusHours(rnd.nextInt(72));
                        int duration = 90 + rnd.nextInt(300); // 1.5h–6.5h
                        LocalDateTime arr = dep.plusMinutes(duration);

                        Flight f = new Flight();
                        f.setAirline("XX"); // demo carrier
                        f.setFlightNumber("XX" + (1000 + i));
                        f.setDepartureAirport(from);
                        f.setArrivalAirport(to);
                        f.setScheduledDeparture(dep);
                        f.setScheduledArrival(arr);
                        f.setStatus(FlightStatus.SCHEDULED);
                        f.setDurationMinutes(duration);
                        f.setDistanceKm(500 + rnd.nextInt(9000)); // rough demo distance

                        flights.add(f);
                    }

                    flightRepo.saveAll(flights);
                }
            }
        };
    }
}
