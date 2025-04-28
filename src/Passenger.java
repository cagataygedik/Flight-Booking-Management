import java.util.ArrayList;
import java.util.List;

class Passenger implements Observer {
    String name;
    private List<Flight> subscribedFlights = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();
    private int loyaltyPoints = 0;

    public Passenger(String name) {
        this.name = name;
    }

    public void subscribeToFlight(Flight flight) {
        flight.addObserver(this);
        subscribedFlights.add(flight);
    }

    public void unsubscribeFromFlight(Flight flight) {
        flight.removeObserver(this);
        subscribedFlights.remove(flight);
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        loyaltyPoints += (int) (booking.getCost() / 10); // Earn 1 point per $10 spent
    }

    public void cancelBooking(Booking booking) {
        bookings.remove(booking);
        unsubscribeFromFlight(booking.getFlight());
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    @Override
    public void update(String message) {
        System.out.println(ConsoleColors.GREEN + name + " received update: " + message + ConsoleColors.RESET);
    }
}