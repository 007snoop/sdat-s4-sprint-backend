package com.sdat_s4_sprint_backend.config;

import com.sdat_s4_sprint_backend.entity.Airport;
import com.sdat_s4_sprint_backend.entity.Flight;
import com.sdat_s4_sprint_backend.entity.FlightStatus;
import com.sdat_s4_sprint_backend.repos.AirportRepository;
import com.sdat_s4_sprint_backend.repos.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;

@Component
public class CsvImportRunner implements CommandLineRunner {

    private final AirportRepository airportRepo;
    private final FlightRepository flightRepo;

    @Autowired
    public CsvImportRunner(AirportRepository airportRepo, FlightRepository flightRepo) {
        this.airportRepo = airportRepo;
        this.flightRepo = flightRepo;
    }

    // Match your current resource paths
    @Value("classpath:data/airports.csv")
    private Resource ourAirportsCsv;

    @Value("classpath:data/routes.dat")
    private Resource routesDat;

    // toggles (optional)
    @Value("${app.csv.import.airports:true}")
    private boolean importAirports;

    @Value("${app.csv.import.generateFlights:true}")
    private boolean generateFlightsFromRoutes;

    @Override
    public void run(String... args) throws Exception {
        if (importAirports && ourAirportsCsv.exists()) {
            importAirportsFromOurAirports();
        }
        if (generateFlightsFromRoutes && routesDat.exists()) {
            generateFlightsForDay(LocalDate.now());
        }
    }

    /** ---- Airports: OurAirports airports.csv ---- */
    private void importAirportsFromOurAirports() throws Exception {
        int created = 0;

        try (var br = new BufferedReader(new InputStreamReader(ourAirportsCsv.getInputStream(), StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) return;

            String[] cols = header.split(",", -1);
            // find column indexes by name
            int idxName = indexOf(cols, "name");
            int idxIata = indexOf(cols, "iata_code");
            int idxIcao = indexOf(cols, "gps_code");
            int idxMunicipality = indexOf(cols, "municipality"); // optional, unused now

            String line;
            while ((line = br.readLine()) != null) {
                String[] t = splitLoose(line, cols.length);

                String iata = val(t, idxIata);
                String icao = val(t, idxIcao);
                String name = val(t, idxName);
                // String cityName = val(t, idxMunicipality); // if/when you wire City

                if (isBlank(name)) continue;
                if (isBlank(iata) && isBlank(icao)) continue;

                String portId = !isBlank(iata) ? iata.trim().toUpperCase(Locale.ROOT)
                        : icao.trim().toUpperCase(Locale.ROOT);

                if (airportRepo.existsByPortId(portId)) continue;

                Airport a = new Airport();
                a.setName(name.trim());
                a.setPortId(portId);
                // a.setCity(...); // optional when we wire City

                airportRepo.save(a);
                created++;
            }
        }
        System.out.println("Imported airports: " + created);
    }

    /** ---- Flights: generate daily instances from OpenFlights routes.dat ---- */
    private void generateFlightsForDay(LocalDate day) throws Exception {
        Random rng = new Random(42);
        int created = 0;

        try (var br = new BufferedReader(new InputStreamReader(routesDat.getInputStream(), StandardCharsets.UTF_8))) {
            String line = br.readLine(); // DELETE this line if your routes.dat has NO header
            while (line != null) {
                String[] t = line.split(",", -1);
                if (t.length < 9) {
                    line = br.readLine();
                    continue;
                }

                String airlineCode = t[0].trim();                  // "AC", "DL", ...
                String srcIata = t[2].trim().toUpperCase(Locale.ROOT);
                String dstIata = t[4].trim().toUpperCase(Locale.ROOT);

                if (isBlank(srcIata) || isBlank(dstIata) || srcIata.equals(dstIata)) {
                    line = br.readLine();
                    continue;
                }

                Airport dep = airportRepo.findByPortId(srcIata).orElse(null);
                Airport arr = airportRepo.findByPortId(dstIata).orElse(null);
                if (dep == null || arr == null) { // not in our airports set yet
                    line = br.readLine();
                    continue;
                }

                int flightsToday = 1 + rng.nextInt(2); // 1–2 flights per route
                for (int i = 0; i < flightsToday; i++) {
                    int hour = 6 + rng.nextInt(14);      // 06:00–19:00
                    int minute = rng.nextBoolean() ? 0 : 30;
                    int blockMins = 60 + rng.nextInt(300);

                    LocalDateTime depTime = day.atTime(hour, minute);
                    LocalDateTime arrTime = depTime.plusMinutes(blockMins);

                    Flight f = new Flight();
                    f.setAirline(airlineCode);
                    f.setFlightNumber(airlineCode + (100 + rng.nextInt(900)));
                    f.setDepartureAirport(dep);
                    f.setArrivalAirport(arr);
                    f.setScheduledDeparture(depTime);
                    f.setScheduledArrival(arrTime);
                    f.setStatus(FlightStatus.SCHEDULED);
                    f.setDistanceKm(null);
                    f.setDurationMinutes(blockMins);

                    flightRepo.save(f);
                    created++;
                }

                line = br.readLine();
            }
        }
        System.out.println("Generated flights for " + day + ": " + created);
    }

    // ---- helpers ----
    private static int indexOf(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (name.equalsIgnoreCase(headers[i].trim())) return i;
        }
        return -1;
    }
    private static String[] splitLoose(String line, int minCols) {
        String[] parts = line.split(",", -1);
        if (parts.length >= minCols) return parts;
        String[] padded = new String[minCols];
        System.arraycopy(parts, 0, padded, 0, parts.length);
        for (int i = parts.length; i < minCols; i++) padded[i] = "";
        return padded;
    }
    private static String val(String[] t, int idx) { return (idx >= 0 && idx < t.length) ? t[idx] : null; }
    private static boolean isBlank(String s){ return s == null || s.trim().isEmpty(); }
}
