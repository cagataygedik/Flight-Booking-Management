import java.io.Serializable;

class Booking implements Serializable {
    private static final long serialVersionUID = 1L;
    transient BookingComponent bookingComponent; // transient because we'll store the details separately
    
    // These fields will be used for serialization
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
        // If deserialized, use stored cost
        if (bookingComponent == null) {
            return cost;
        }
        return bookingComponent.getCost();
    }
    
    public String getDescription() {
        // If deserialized, use stored description
        if (bookingComponent == null) {
            return description;
        }
        return bookingComponent.getDescription();
    }
    
    public Flight getFlight() {
        // If deserialized, we need to look up the flight from the database
        if (bookingComponent == null) {
            FlightDatabase db = new FlightDatabase();
            return db.getFlightByNumber(flightNumber);
        }
        return bookingComponent.getFlight();
    }
    
    // When booking component is modified, update the serializable fields
    public void setBookingComponent(BookingComponent component) {
        this.bookingComponent = component;
        this.description = component.getDescription();
        this.cost = component.getCost();
        this.flightNumber = component.getFlight().getFlightNumber();
    }
}