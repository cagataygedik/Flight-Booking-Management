import java.util.ArrayList;
import java.util.List;

class Passenger implements Observer {
    String name;
    private List<Flight> subscribedFlights = new ArrayList<>();

    public Passenger(String name) {
        this.name = name;
    }

    public void subscribeToFlight(Flight flight) {
        flight.addObserver(this);
        subscribedFlights.add(flight);
    }

    @Override
    public void update(String message) {
        System.out.println(name + " received update: " + message);
    }
}