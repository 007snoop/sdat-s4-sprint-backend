package com.sdat_s4_sprint_backend.config;

import com.sdat_s4_sprint_backend.entity.*;
import com.sdat_s4_sprint_backend.repos.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
            FlightRepository flightRepo,
            AircraftRepository aircraftRepo,
            PassengerRepository passengerRepo
    ) {
        return args -> {
            if (!seedEnabled()) return;

            /* -------------------- AIRPORTS + CITIES -------------------- */
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

            seedAirportsAndCities(airportsSeed, airportRepo, cityRepo);

            /* -------------------- AIRCRAFT -------------------- */
            List<Object[]> aircraftSeed = List.of(
                    new Object[]{"C-FABC", "A320-200", "Airbus", 180},
                    new Object[]{"N12345", "B737-800", "Boeing", 172},
                    new Object[]{"G-ABCD", "A321neo", "Airbus", 220},
                    new Object[]{"D-EF12", "E190", "Embraer", 114},
                    new Object[]{"JA777A", "B777-300ER", "Boeing", 396}
            );
            seedAircraft(aircraftSeed, aircraftRepo);

            /* -------------------- PASSENGERS -------------------- */
            List<Object[]> paxSeed = List.of(
                    new Object[]{"Ava", "Nguyen", "ava.nguyen@example.com"},
                    new Object[]{"Liam", "Patel", "liam.patel@example.com"},
                    new Object[]{"Noah", "Smith", "noah.smith@example.com"},
                    new Object[]{"Olivia", "Johnson", "olivia.johnson@example.com"},
                    new Object[]{"Mia", "Garcia", "mia.garcia@example.com"}
            );
            seedPassengers(paxSeed, passengerRepo, airportRepo);

            /* -------------------- FLIGHTS -------------------- */
            backfillFlights(flightRepo);
            seedDemoFlightsIfEmpty(flightRepo, airportRepo);
        };
    }

    /* ================================== AIRPORTS ================================== */

    private void seedAirportsAndCities(
            List<String[]> airportsSeed,
            AirportRepository airportRepo,
            CityRepository cityRepo
    ) {
        // Build quick in-DB lookups (case-insensitive name map)
        List<Airport> all = airportRepo.findAll();
        Map<String, Airport> byCode = all.stream()
                .filter(a -> a.getPortId() != null)
                .collect(Collectors.toMap(Airport::getPortId, a -> a, (a,b)->a));
        Map<String, Airport> byName = all.stream()
                .collect(Collectors.toMap(a -> a.getName().toLowerCase(Locale.ROOT), a -> a, (a,b)->a));

        // Backfill any existing airports missing portId using name match
        for (Airport a : all) {
            if (a.getPortId() == null) {
                airportsSeed.stream()
                        .filter(row -> a.getName().equalsIgnoreCase(row[1]))
                        .findFirst()
                        .ifPresent(row -> {
                            a.setPortId(row[0]);
                            airportRepo.save(a);
                            byCode.put(row[0], a);
                        });
            }
        }

        // Ensure each seed airport exists exactly once (idempotent)
        for (String[] row : airportsSeed) {
            String code = row[0];
            String name = row[1];
            String cityName = row[2];
            String country = row[3];

            Airport existing = requeryAirportByCode(airportRepo, code);
            if (existing == null) existing = requeryAirportByName(airportRepo, name);

            if (existing == null) {
                City city = requeryOrCreateCity(cityRepo, cityName, country);
                Airport ap = new Airport();
                ap.setName(name);
                ap.setPortId(code);
                ap.setCity(city);
                airportRepo.save(ap);
            } else {
                boolean changed = false;
                if (existing.getPortId() == null || !code.equals(existing.getPortId())) {
                    existing.setPortId(code); changed = true;
                }
                if (!Objects.equals(existing.getName(), name)) {
                    existing.setName(name); changed = true;
                }
                changed |= syncCity(existing, cityName, country, cityRepo);
                if (changed) airportRepo.save(existing);
            }
        }
    }

    private City requeryOrCreateCity(CityRepository cityRepo, String name, String countryCode) {
        return cityRepo.findByNameIgnoreCase(name).orElseGet(() -> {
            City c = new City();
            c.setName(name);
            c.setProvince(countryCode); // stash country in province for now
            c.setPopulation(0);
            return cityRepo.save(c);
        });
    }

    private Airport requeryAirportByCode(AirportRepository repo, String code) {
        if (code == null) return null;
        return repo.findAll().stream()
                .filter(a -> code.equals(a.getPortId()))
                .findFirst().orElse(null);
    }

    private Airport requeryAirportByName(AirportRepository repo, String name) {
        if (name == null) return null;
        String n = name.toLowerCase(Locale.ROOT);
        return repo.findAll().stream()
                .filter(a -> a.getName() != null && a.getName().toLowerCase(Locale.ROOT).equals(n))
                .findFirst().orElse(null);
    }

    /** Ensure city attached and aligned with seed; return true if airport changed. */
    private boolean syncCity(Airport ap, String cityName, String countryCode, CityRepository cityRepo) {
        City city = requeryOrCreateCity(cityRepo, cityName, countryCode);
        if (ap.getCity() == null || !Objects.equals(ap.getCity().getId(), city.getId())) {
            ap.setCity(city);
            return true;
        }
        return false;
    }

    /* ================================== AIRCRAFT ================================== */

    private void seedAircraft(List<Object[]> aircraftSeed, AircraftRepository aircraftRepo) {
        // build existing registrations from DB using flexible getters
        Map<String, Aircraft> byReg = new HashMap<>();
        for (Aircraft a : aircraftRepo.findAll()) {
            String reg = firstNonNull(
                    getStringProp(a, "getRegistration"),
                    getStringProp(a, "getTailNumber"),
                    getStringProp(a, "getCode")
            );
            if (reg != null) byReg.put(reg.toUpperCase(Locale.ROOT), a);
        }

        for (Object[] row : aircraftSeed) {
            String reg = ((String) row[0]).toUpperCase(Locale.ROOT);
            String model = (String) row[1];
            String maker = (String) row[2];
            Integer cap = (Integer) row[3];

            Aircraft existing = byReg.get(reg);
            if (existing == null) {
                // re-check DB just before insert (helps when multiple instances start)
                existing = aircraftRepo.findAll().stream()
                        .filter(a -> reg.equalsIgnoreCase(
                                firstNonNull(getStringProp(a, "getRegistration"),
                                        getStringProp(a, "getTailNumber"),
                                        getStringProp(a, "getCode"))))
                        .findFirst().orElse(null);
            }

            if (existing == null) {
                Aircraft a = new Aircraft();
                setIfPresent(a, "setRegistration", String.class, reg);
                setIfPresent(a, "setTailNumber", String.class, reg); // alt
                setIfPresent(a, "setCode", String.class, reg);       // alt

                setIfPresent(a, "setModel", String.class, model);
                setIfPresent(a, "setType", String.class, model);     // alt
                setIfPresent(a, "setAircraftModel", String.class, model);

                setIfPresent(a, "setManufacturer", String.class, maker);
                setIfPresent(a, "setMaker", String.class, maker);    // alt

                setIfPresent(a, "setCapacity", Integer.class, cap);
                setIfPresent(a, "setSeats", Integer.class, cap);     // alt

                aircraftRepo.save(a);
                byReg.put(reg, a);
            } else {
                boolean changed = false;
                changed |= setIfChanged(existing, "setModel", String.class, model, "getModel");
                changed |= setIfChanged(existing, "setType", String.class, model, "getType");
                changed |= setIfChanged(existing, "setManufacturer", String.class, maker, "getManufacturer");
                changed |= setIfChanged(existing, "setMaker", String.class, maker, "getMaker");
                changed |= setIfChanged(existing, "setCapacity", Integer.class, cap, "getCapacity");
                changed |= setIfChanged(existing, "setSeats", Integer.class, cap, "getSeats");
                if (changed) aircraftRepo.save(existing);
            }
        }
    }

    /* ================================== PASSENGERS ================================== */

    private void seedPassengers(
            List<Object[]> paxSeed,
            PassengerRepository passengerRepo,
            AirportRepository airportRepo
    ) {
        // Build existing emails/usernames
        Map<String, Passenger> byEmail = new HashMap<>();
        for (Passenger p : passengerRepo.findAll()) {
            String email = firstNonNull(getStringProp(p, "getEmail"),
                    getStringProp(p, "getUsername"));
            if (email != null) byEmail.put(email.toLowerCase(Locale.ROOT), p);
        }

        List<Airport> airports = airportRepo.findAll();
        Random rnd = new Random(99);

        for (Object[] row : paxSeed) {
            String first = (String) row[0];
            String last  = (String) row[1];
            String email = (String) row[2];

            Passenger existing = email == null ? null : byEmail.get(email.toLowerCase(Locale.ROOT));
            if (existing == null && email != null) {
                existing = passengerRepo.findAll().stream()
                        .filter(p -> email.equalsIgnoreCase(firstNonNull(getStringProp(p, "getEmail"),
                                getStringProp(p, "getUsername"))))
                        .findFirst().orElse(null);
            }

            if (existing == null) {
                Passenger p = new Passenger();
                setIfPresent(p, "setFirstName", String.class, first);
                setIfPresent(p, "setGivenName", String.class, first);
                setIfPresent(p, "setLastName", String.class, last);
                setIfPresent(p, "setFamilyName", String.class, last);
                setIfPresent(p, "setEmail", String.class, email);
                setIfPresent(p, "setUsername", String.class, email);

                // Optionally attach to 1–2 airports if the relation exists
                try {
                    Method getter = p.getClass().getMethod("getAirports");
                    Object coll = getter.invoke(p);
                    if (coll instanceof Collection<?> collection && !airports.isEmpty()) {
                        int count = 1 + rnd.nextInt(Math.min(2, airports.size()));
                        for (int i = 0; i < count; i++) {
                            Airport a = airports.get(rnd.nextInt(airports.size()));
                            ((Collection) collection).add(a);
                        }
                    }
                } catch (Exception ignored) {}

                passengerRepo.save(p);
                if (email != null) byEmail.put(email.toLowerCase(Locale.ROOT), p);
            } else {
                boolean changed = false;
                changed |= setIfChanged(existing, "setFirstName", String.class, first, "getFirstName");
                changed |= setIfChanged(existing, "setGivenName", String.class, first, "getGivenName");
                changed |= setIfChanged(existing, "setLastName", String.class, last, "getLastName");
                changed |= setIfChanged(existing, "setFamilyName", String.class, last, "getFamilyName");
                if (changed) passengerRepo.save(existing);
            }
        }
    }

    /* ================================== FLIGHTS ================================== */

    private void backfillFlights(FlightRepository flightRepo) {
        List<Flight> flights = flightRepo.findAll();
        if (flights.isEmpty()) return;

        Random rnd = new Random(4242);
        for (Flight f : flights) {
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
        flightRepo.saveAll(flights);
    }

    private void seedDemoFlightsIfEmpty(FlightRepository flightRepo, AirportRepository airportRepo) {
        if (!flightRepo.findAll().isEmpty()) return;

        List<Airport> airports = airportRepo.findAll();
        if (airports.size() < 2) return;

        Random rnd = new Random(42);
        List<Flight> flights = new ArrayList<>(40);
        for (int i = 0; i < 40; i++) {
            Airport from = airports.get(rnd.nextInt(airports.size()));
            Airport to;
            do { to = airports.get(rnd.nextInt(airports.size())); }
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

    /* =============================== reflection utils =============================== */

    private static <T> void setIfPresent(Object target, String setter, Class<T> type, T value) {
        if (value == null) return;
        try {
            Method m = target.getClass().getMethod(setter, type);
            m.invoke(target, value);
        } catch (Exception ignored) {}
    }

    private static String getStringProp(Object target, String getter) {
        try {
            Method m = target.getClass().getMethod(getter);
            Object v = m.invoke(target);
            return v == null ? null : String.valueOf(v);
        } catch (Exception ignored) {
            return null;
        }
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... vals) {
        for (T v : vals) if (v != null) return v;
        return null;
    }

    private static <T> boolean setIfChanged(Object target, String setter, Class<T> type, T value, String getter) {
        if (value == null) return false;
        try {
            Method g = target.getClass().getMethod(getter);
            Object current = g.invoke(target);
            if (!Objects.equals(current, value)) {
                Method s = target.getClass().getMethod(setter, type);
                s.invoke(target, value);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
