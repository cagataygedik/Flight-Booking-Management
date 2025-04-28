import java.util.ArrayList;
import java.util.List;

class Flight implements Observable {
    private String flightNumber;
    private String departure;
    private String arrival;
    private String status;
    private List<Observer> observers = new ArrayList<>();

    public Flight(String flightNumber, String departure, String arrival) {
        this.flightNumber = flightNumber;
        this.departure = departure;
        this.arrival = arrival;
        this.status = "On Time";
    }

    public void setStatus(String status) {
        this.status = status;
        notifyObservers();
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update("Flight " + flightNumber + " status updated to: " + status);
        }
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getDeparture() {
        return departure;
    }

    public String getArrival() {
        return arrival;
    }

    @Override
    public String toString() {
        return "Flight " + flightNumber + " from " + departure + " to " + arrival + " - Status: " + status;
    }
}
