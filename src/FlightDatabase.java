import java.util.ArrayList;
import java.util.List;

class FlightDatabase {
    private List<Flight> flights = new ArrayList<>();

    public FlightDatabase() {
        // Sample flights
        flights.add(new Flight("FL123", "New York", "London"));
        flights.add(new Flight("FL456", "Paris", "Tokyo"));
        flights.add(new Flight("FL789", "Sydney", "Dubai"));
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
}