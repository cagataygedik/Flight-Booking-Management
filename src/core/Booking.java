package core;

import java.io.Serializable;
import data.FlightDatabase;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;
    public transient BookingComponent bookingComponent; 
    
    
    private String description;
    private double cost;
    private String flightNumber;
    
    public Booking(BookingComponent bookingComponent) {
        this.bookingComponent = bookingComponent;
        this.description = bookingComponent.getDescription();
        this.cost = bookingComponent.getCost();
        this.flightNumber = bookingComponent.getFlight().getFlightNumber();
    }
    
    public double getCost() {
        
        if (bookingComponent == null) {
            return cost;
        }
        return bookingComponent.getCost();
    }
    
    public String getDescription() {
        
        if (bookingComponent == null) {
            return description;
        }
        return bookingComponent.getDescription();
    }
    
    public Flight getFlight() {
        
        if (bookingComponent == null) {
            FlightDatabase db = new FlightDatabase();
            return db.getFlightByNumber(flightNumber);
        }
        return bookingComponent.getFlight();
    }
    
    
    public void setBookingComponent(BookingComponent component) {
        this.bookingComponent = component;
        this.description = component.getDescription();
        this.cost = component.getCost();
        this.flightNumber = component.getFlight().getFlightNumber();
    }
}