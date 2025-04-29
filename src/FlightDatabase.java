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

class FlightDatabase {
    private List<Flight> flights = new ArrayList<>();
    private static final String CSV_FILE_PATH = "data/data.csv";

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
}