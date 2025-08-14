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
import java.util.stream.Collectors;

/**
 * Demo seed that is SAFE to re-run.
 * - APP_SEED_ENABLED (default true) toggles seeding.
 * - Backfills legacy rows:
 *      * Airports with null portId (matched by name).
 *      * Flights with null times/airline/duration/distance.
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

            // -------- Seed source (IATA, airportName, cityName, countryCode) --------
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

            // Quick lookups from seed
            Map<String,String[]> seedByName =
                    airportsSeed.stream().collect(Collectors.toMap(a -> a[1].toLowerCase(Locale.ROOT), a -> a));
            Map<String,String[]> seedByCode =
                    airportsSeed.stream().collect(Collectors.toMap(a -> a[0], a -> a));

            // Existing DB lookups
            List<Airport> allAirports = airportRepo.findAll();
            Map<String,Airport> dbByPortId = allAirports.stream()
                    .filter(a -> a.getPortId() != null)
                    .collect(Collectors.toMap(Airport::getPortId, a -> a, (a,b) -> a));
            Map<String,Airport> dbByName = allAirports.stream()
                    .collect(Collectors.toMap(a -> a.getName().toLowerCase(Locale.ROOT), a -> a, (a,b) -> a));

            // -------- 1) BACKFILL existing airports with null portId (match by name) --------
            for (Airport a : allAirports) {
                if (a.getPortId() == null) {
                    String[] seed = seedByName.get(a.getName().toLowerCase(Locale.ROOT));
                    if (seed != null) {
                        a.setPortId(seed[0]); // IATA code
                        airportRepo.save(a);
                        dbByPortId.put(seed[0], a);
                    }
                }
            }

            // -------- 2) Ensure all seed airports exist (create missing) --------
            for (String[] row : airportsSeed) {
                String code = row[0];
                String apName = row[1];
                String cityName = row[2];
                String countryCode = row[3];

                Airport existing = dbByPortId.get(code);
                if (existing == null) {
                    // Try to attach to an existing airport by name, otherwise create new.
                    Airport byName = dbByName.get(apName.toLowerCase(Locale.ROOT));
                    if (byName != null) {
                        if (byName.getPortId() == null || !code.equals(byName.getPortId())) {
                            byName.setPortId(code);
                        }
                        syncCity(byName, cityName, countryCode, cityRepo);
                        airportRepo.save(byName);
                        dbByPortId.put(code, byName);
                        continue;
                    }

                    // Create fresh
                    City city = cityRepo.findByNameIgnoreCase(cityName).orElseGet(() -> {
                        City c = new City();
                        c.setName(cityName);
                        c.setProvince(countryCode); // stash country code here
                        c.setPopulation(0);
                        return cityRepo.save(c);
                    });

                    Airport ap = new Airport();
                    ap.setName(apName);
                    ap.setPortId(code);
                    ap.setCity(city);
                    airportRepo.save(ap);

                    dbByPortId.put(code, ap);
                    dbByName.put(apName.toLowerCase(Locale.ROOT), ap);
                } else {
                    // Keep name/city in sync with seed
                    boolean changed = false;
                    if (!Objects.equals(existing.getName(), apName)) {
                        existing.setName(apName);
                        changed = true;
                    }
                    changed |= syncCity(existing, cityName, countryCode, cityRepo);
                    if (changed) airportRepo.save(existing);
                }
            }

            // Refresh airport list for flight generation
            allAirports = airportRepo.findAll();

            // -------- 3) BACKFILL legacy flights (null times/airline/duration/distance) --------
            List<Flight> existingFlights = flightRepo.findAll();
            if (!existingFlights.isEmpty()) {
                Random rnd = new Random(4242);
                for (Flight f : existingFlights) {
                    if (f.getAirline() == null) f.setAirline("XX");
                    if (f.getDurationMinutes() == null) f.setDurationMinutes(90 + rnd.nextInt(300));
                    if (f.getDistanceKm() == null) f.setDistanceKm(500 + rnd.nextInt(9000));
                    if (f.getScheduledDeparture() == null || f.getScheduledArrival() == null) {
                        LocalDateTime dep = LocalDateTime.now().plusHours(rnd.nextInt(72));
                        LocalDateTime arr = dep.plusMinutes(f.getDurationMinutes());
                        f.setScheduledDeparture(dep);
                        f.setScheduledArrival(arr);
                    }
                    if (f.getStatus() == null) f.setStatus(FlightStatus.SCHEDULED);
                }
                flightRepo.saveAll(existingFlights);
            }

            // -------- 4) Seed demo flights ONLY if table empty --------
            if (existingFlights.isEmpty() && allAirports.size() >= 2) {
                Random rnd = new Random(42);
                List<Flight> flights = new ArrayList<>(40);
                for (int i = 0; i < 40; i++) {
                    Airport from = allAirports.get(rnd.nextInt(allAirports.size()));
                    Airport to;
                    do { to = allAirports.get(rnd.nextInt(allAirports.size())); }
                    while (Objects.equals(from.getId(), to.getId()));

                    int duration = 90 + rnd.nextInt(300);
                    LocalDateTime dep = LocalDateTime.now().plusHours(rnd.nextInt(72));
                    LocalDateTime arr = dep.plusMinutes(duration);

                    Flight f = new Flight();
                    f.setAirline("XX");
                    f.setFlightNumber("XX" + (1000 + i));
                    f.setDepartureAirport(from);
                    f.setArrivalAirport(to);
                    f.setScheduledDeparture(dep);
                    f.setScheduledArrival(arr);
                    f.setStatus(FlightStatus.SCHEDULED);
                    f.setDurationMinutes(duration);
                    f.setDistanceKm(500 + rnd.nextInt(9000));
                    flights.add(f);
                }
                flightRepo.saveAll(flights);
            }
        };
    }

    /** Ensure city attached and aligned with seed; return true if airport changed. */
    private boolean syncCity(Airport ap, String cityName, String countryCode, CityRepository cityRepo) {
        City city = cityRepo.findByNameIgnoreCase(cityName).orElseGet(() -> {
            City c = new City();
            c.setName(cityName);
            c.setProvince(countryCode);
            c.setPopulation(0);
            return cityRepo.save(c);
        });
        if (ap.getCity() == null || !Objects.equals(ap.getCity().getId(), city.getId())) {
            ap.setCity(city);
            return true;
        }
        return false;
    }
}
