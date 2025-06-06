package core;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import patterns.observer.Observer;
import ui.ConsoleColors;

public class Passenger implements Observer, Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    private transient List<Flight> subscribedFlights = new ArrayList<>(); 
    private List<Booking> bookings = new ArrayList<>();
    private int loyaltyPoints = 0;

    public Passenger(String name) {
        this.name = name;
    }

    public void subscribeToFlight(Flight flight) {
        
        if (subscribedFlights == null) {
            subscribedFlights = new ArrayList<>();
        }
        flight.addObserver(this);
        subscribedFlights.add(flight);
    }

    public void unsubscribeFromFlight(Flight flight) {
        
        if (subscribedFlights == null) {
            subscribedFlights = new ArrayList<>();
            return; 
        }
        flight.removeObserver(this);
        subscribedFlights.remove(flight);
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        loyaltyPoints += (int) (booking.getCost() / 10); 
    }

    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
    }

    public void cancelBooking(Booking booking) {
        bookings.remove(booking);
        unsubscribeFromFlight(booking.getFlight());
    }

    public boolean cancelGroupBooking(GroupBooking groupBooking) {
        
        boolean removed = groupBooking.removePassenger(this);
        
        
        if (removed) {
            unsubscribeFromFlight(groupBooking.getFlight());
        }
        
        return removed;
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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Passenger other = (Passenger) obj;
        return name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}