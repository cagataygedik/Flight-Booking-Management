import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

class FlightDatabase {
    private List<Flight> flights = new ArrayList<>();
    private static final String CSV_FILE_PATH = "Flight-Booking-Management/data/data.csv";

    public FlightDatabase() {
        loadFlightsFromCSV();
    }

    private void loadFlightsFromCSV() {
        Path path = Paths.get(CSV_FILE_PATH);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line = br.readLine();
            if (line == null) {
                System.err.println(ConsoleColors.RED + "CSV file is empty: " + path.toAbsolutePath() + ConsoleColors.RESET);
                return;
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 8) {
                    try {
                        String flightNumber = values[0].trim();
                        String departure = values[1].trim();
                        String arrival = values[2].trim();
                        String airline = values[3].trim();
                        LocalDate date = LocalDate.parse(values[4].trim(), dateFormatter);
                        LocalTime time = LocalTime.parse(values[5].trim(), timeFormatter);
                        double duration = Double.parseDouble(values[6].trim());
                        double price = Double.parseDouble(values[7].trim());

                        flights.add(new Flight(flightNumber, departure, arrival, airline, date, time, duration, price));
                    } catch (DateTimeParseException | NumberFormatException e) {
                        System.err.println(ConsoleColors.RED + "Skipping invalid row: " + line + " - Error: " + e.getMessage() + ConsoleColors.RESET);
                    }
                } else {
                     System.err.println(ConsoleColors.YELLOW + "Skipping malformed row (expected 8 columns): " + line + ConsoleColors.RESET);
                }
            }
             System.out.println(ConsoleColors.GREEN + "Loaded " + flights.size() + " flights from " + path.getFileName() + ConsoleColors.RESET);

        } catch (IOException e) {
            System.err.println(ConsoleColors.RED + "Error loading flights from CSV: " + path.toAbsolutePath() + " - " + e.getMessage() + ConsoleColors.RESET);
        }
    }


    public List<Flight> searchFlights(String departure, String arrival) {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getDeparture().equalsIgnoreCase(departure) && flight.getArrival().equalsIgnoreCase(arrival)) {
                result.add(flight);
            }
        }
        return result;
    }

    public Flight getFlightByNumber(String flightNumber) {
        for (Flight flight : flights) {
            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                return flight;
            }
        }
        return null;
    }

    public boolean updateFlightStatus(String flightNumber, String newStatus) {
        Flight flight = getFlightByNumber(flightNumber);
        if (flight != null) {
            flight.setStatus(newStatus);
            return true;
        }
        return false;
    }
    /**
     * Gets a sorted list of unique departure city codes.
     * @return List of unique departure cities.
     */
    public List<String> getAvailableDepartures() {
        Set<String> departures = new TreeSet<>(); // Use TreeSet for automatic sorting and uniqueness
        for (Flight flight : flights) {
            departures.add(flight.getDeparture());
        }
        return new ArrayList<>(departures);
    }

    /**
     * Gets a sorted list of unique arrival city codes.
     * @return List of unique arrival cities.
     */
    public List<String> getAvailableArrivals() {
        Set<String> arrivals = new TreeSet<>(); // Use TreeSet for automatic sorting and uniqueness
        for (Flight flight : flights) {
            arrivals.add(flight.getArrival());
        }
        return new ArrayList<>(arrivals);
    }

    /**
     * Gets a sorted list of unique arrival city codes available from a specific departure city.
     * @param departureCity The departure city code.
     * @return List of unique arrival cities from the given departure.
     */
    public List<String> getAvailableArrivalsFrom(String departureCity) {
        return flights.stream()
                      .filter(f -> f.getDeparture().equalsIgnoreCase(departureCity))
                      .map(Flight::getArrival)
                      .distinct()
                      .sorted()
                      .collect(Collectors.toList());
    }
    
    /**
     * Gets a list of airlines available in the database.
     * @return List of unique airlines.
     */
    public List<String> getAvailableAirlines() {
        Set<String> airlines = new TreeSet<>(); // Use TreeSet for automatic sorting and uniqueness
        for (Flight flight : flights) {
            airlines.add(flight.getAirline());
        }
        return new ArrayList<>(airlines);
    }
    
    /**
     * Gets flights between a date range.
     * @param startDate The start date (inclusive).
     * @param endDate The end date (inclusive).
     * @return List of flights departing within the date range.
     */
    public List<Flight> getFlightsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Flight> results = new ArrayList<>();
        for (Flight flight : flights) {
            LocalDate departureDate = flight.getDepartureDate();
            if ((departureDate.isEqual(startDate) || departureDate.isAfter(startDate)) &&
                (departureDate.isEqual(endDate) || departureDate.isBefore(endDate))) {
                results.add(flight);
            }
        }
        return results;
    }
    
    /**
     * Gets flights with a price less than or equal to the maximum price.
     * @param maxPrice The maximum price.
     * @return List of flights with price <= maxPrice.
     */
    public List<Flight> getFlightsByMaxPrice(double maxPrice) {
        List<Flight> results = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getPrice() <= maxPrice) {
                results.add(flight);
            }
        }
        return results;
    }
    
    /**
     * Gets flights for a specific airline.
     * @param airline The airline name.
     * @return List of flights operated by the airline.
     */
    public List<Flight> getFlightsByAirline(String airline) {
        List<Flight> results = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getAirline().equalsIgnoreCase(airline)) {
                results.add(flight);
            }
        }
        return results;
    }
    
    /**
     * Gets flights with a duration less than or equal to the maximum duration.
     * @param maxDuration The maximum duration in hours.
     * @return List of flights with duration <= maxDuration.
     */
    public List<Flight> getFlightsByMaxDuration(double maxDuration) {
        List<Flight> results = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getDuration() <= maxDuration) {
                results.add(flight);
            }
        }
        return results;
    }
    
    /**
     * Gets all flights in the database.
     * @return List of all flights.
     */
    public List<Flight> getAllFlights() {
        return new ArrayList<>(flights);
    }
}