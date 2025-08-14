package com.sdat_s4_sprint_backend.config;

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

    // ---- tiny reflection helpers: call the first matching setter if present ----
    private static boolean trySet(Object target, Object value, String... setterNames) {
        for (String name : setterNames) {
            for (Method m : target.getClass().getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == 1) {
                    Class<?> p = m.getParameterTypes()[0];
                    try {
                        if (value == null || p.isAssignableFrom(value.getClass())) {
                            m.invoke(target, value);
                            return true;
                        }
                        // primitive wrappers
                        if (p == int.class && value instanceof Number v) { m.invoke(target, v.intValue()); return true; }
                        if (p == long.class && value instanceof Number v) { m.invoke(target, v.longValue()); return true; }
                        if (p == double.class && value instanceof Number v) { m.invoke(target, v.doubleValue()); return true; }
                    } catch (Exception ignore) {}
                }
            }
        }
        return false;
    }

    @Bean
    ApplicationRunner seed(AirportRepository airportRepo, FlightRepository flightRepo) {
        return args -> {
            if (airportRepo.count() == 0) {
                // code, name, city, country (we’ll set what exists)
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

                List<Object> toSave = new ArrayList<>();
                for (String[] a : data) {
                    Object airport = create(airportRepo); // new Airport()
                    // Try common field names; missing ones will be ignored
                    trySet(airport, a[0], "setCode", "setIata", "setIataCode", "setAirportCode");
                    trySet(airport, a[1], "setName");
                    trySet(airport, a[2], "setCity", "setCityName");
                    trySet(airport, a[3], "setCountry", "setCountryCode");
                    toSave.add(airport);
                }
                // saveAll(Iterable<?>) exists on CrudRepository/JpaRepository
                airportRepo.saveAll((Iterable) toSave);
            }

            if (flightRepo.count() == 0) {
                var airports = airportRepo.findAll();
                if (airports.size() < 2) return;

                Random rnd = new Random(42);
                List<Object> flights = new ArrayList<>();

                for (int i = 0; i < 40; i++) {
                    Object flight = create(flightRepo); // new Flight()

                    // code
                    trySet(flight, "FL" + (1000 + i), "setCode", "setFlightCode", "setNumber", "setFlightNumber");

                    // pick two distinct airports
                    Object from = airports.get(rnd.nextInt(airports.size()));
                    Object to;
                    do { to = airports.get(rnd.nextInt(airports.size())); } while (sameEntity(from, to));

                    // set relations (we cover common names)
                    trySet(flight, from,
                            "setFromAirport", "setOrigin", "setSourceAirport", "setDepartureAirport");
                    trySet(flight, to,
                            "setToAirport", "setDestination", "setDestAirport", "setArrivalAirport");

                    // times
                    LocalDateTime dep = LocalDateTime.now().plusHours(rnd.nextInt(72));
                    LocalDateTime arr = dep.plusHours(2 + rnd.nextInt(10));
                    trySet(flight, dep, "setDepartureTime", "setDepartAt", "setDepartsAt");
                    trySet(flight, arr, "setArrivalTime", "setArriveAt", "setArrivesAt");

                    // status (enum or String)
                    boolean setEnum = trySet(flight, FlightStatus.SCHEDULED, "setStatus");
                    if (!setEnum) trySet(flight, "SCHEDULED", "setStatus", "setState");

                    flights.add(flight);
                }
                flightRepo.saveAll((Iterable) flights);
            }
        };
    }

    // Create a new instance of the entity managed by this repository
    private static Object create(Object repo) {
        // CrudRepository<X, ?> has a domain type X accessible via generic at runtime? Not reliably.
        // We’ll try common entity class names based on repo package name.
        // Simpler: use the first method parameter type of save(..).
        for (Method m : repo.getClass().getMethods()) {
            if (m.getName().equals("save") && m.getParameterCount() == 1) {
                try { return m.getParameterTypes()[0].getDeclaredConstructor().newInstance(); }
                catch (Exception ignore) {}
            }
        }
        throw new IllegalStateException("Cannot create entity instance for repo: " + repo.getClass());
    }

    private static boolean sameEntity(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        try {
            Method getIdA = a.getClass().getMethod("getId");
            Method getIdB = b.getClass().getMethod("getId");
            Object ida = getIdA.invoke(a);
            Object idb = getIdB.invoke(b);
            return Objects.equals(ida, idb);
        } catch (Exception e) {
            return a.equals(b);
        }
    }
}
