package com.sdat_s4_sprint_backend.config;

import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.entity.Flight;
import com.sdat_s4_sprint_backend.entity.FlightStatus;
import com.sdat_s4_sprint_backend.repos.AirportRepository;
import com.sdat_s4_sprint_backend.repos.FlightRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
public class SeedDataRunner {

    // Toggle with env APP_SEED_ENABLED=true (default true). Seeds only when tables empty.
    private boolean seedEnabled() {
        String v = System.getenv("APP_SEED_ENABLED");
        return v == null || Boolean.parseBoolean(v);
    }

    // Try a list of setter names; skip silently if not present
    private static boolean trySet(Object target, Object value, String... setterNames) {
        for (String name : setterNames) {
            for (Method m : target.getClass().getMethods()) {
                if (!m.getName().equals(name) || m.getParameterCount() != 1) continue;
                Class<?> p = m.getParameterTypes()[0];
                try {
                    if (value == null || p.isAssignableFrom(value.getClass())) {
                        m.invoke(target, value);
                        return true;
                    }
                    if (p == int.class && value instanceof Number n) { m.invoke(target, n.intValue()); return true; }
                    if (p == long.class && value instanceof Number n) { m.invoke(target, n.longValue()); return true; }
                    if (p == double.class && value instanceof Number n) { m.invoke(target, n.doubleValue()); return true; }
                } catch (Exception ignore) {}
            }
        }
        return false;
    }

    @Bean
    ApplicationRunner seed(AirportRepository airportRepo, FlightRepository flightRepo) {
        return args -> {
            if (!seedEnabled()) return;

            if (airportRepo.count() == 0) {
                // code, name, city, country
                List<String[]> data = List.of(
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

                List<Airport> toSave = new ArrayList<>();
                for (String[] a : data) {
                    Airport ap = new Airport(); // << concrete entity, not Object
                    trySet(ap, a[0], "setCode", "setIata", "setIataCode", "setAirportCode");
                    trySet(ap, a[1], "setName");
                    trySet(ap, a[2], "setCity", "setCityName");
                    trySet(ap, a[3], "setCountry", "setCountryCode");
                    toSave.add(ap);
                }
                airportRepo.saveAll(toSave);
            }

            if (flightRepo.count() == 0) {
                List<Airport> airports = airportRepo.findAll();
                if (airports.size() < 2) return;

                Random rnd = new Random(42);
                List<Flight> flights = new ArrayList<>();

                for (int i = 0; i < 40; i++) {
                    Flight f = new Flight(); // << concrete entity

                    // identifiers / number
                    trySet(f, "FL" + (1000 + i), "setCode", "setFlightCode", "setNumber", "setFlightNumber");

                    // from / to
                    Airport from = airports.get(rnd.nextInt(airports.size()));
                    Airport to;
                    do { to = airports.get(rnd.nextInt(airports.size())); } while (Objects.equals(from.getId(), to.getId()));

                    trySet(f, from, "setFromAirport", "setOrigin", "setSourceAirport", "setDepartureAirport");
                    trySet(f, to,   "setToAirport",   "setDestination", "setDestAirport", "setArrivalAirport");

                    // times
                    LocalDateTime dep = LocalDateTime.now().plusHours(rnd.nextInt(72));
                    LocalDateTime arr = dep.plusHours(2 + rnd.nextInt(10));
                    trySet(f, dep, "setDepartureTime", "setDepartAt", "setDepartsAt");
                    trySet(f, arr, "setArrivalTime",   "setArriveAt",  "setArrivesAt");

                    // status
                    boolean setEnum = trySet(f, FlightStatus.SCHEDULED, "setStatus");
                    if (!setEnum) trySet(f, "SCHEDULED", "setStatus", "setState");

                    flights.add(f);
                }
                flightRepo.saveAll(flights);
            }
        };
    }
}
