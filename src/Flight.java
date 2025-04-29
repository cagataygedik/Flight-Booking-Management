import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

class Flight implements Observable {
    private String flightNumber;
    private String departure;
    private String arrival;
    private String airline;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private double duration;
    private double price;
    private double originalPrice;
    private int discountPercentage;
    private String status;
    private List<Observer> observers = new ArrayList<>();


    public Flight(String flightNumber, String departure, String arrival, String airline,
                  LocalDate departureDate, LocalTime departureTime, double duration, double price) {
        this.flightNumber = flightNumber;
        this.departure = departure;
        this.arrival = arrival;
        this.airline = airline;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.duration = duration;
        this.price = price;
        this.originalPrice = price;
        this.discountPercentage = 0;
        this.status = "On Time";
    }

    public void setSpecialOffer(int discountPercentage, double newPrice) {
        this.discountPercentage = discountPercentage;
        this.price = newPrice;
        
        // Notify observers about the special offer
        String message = String.format("Special offer for Flight %s: %d%% discount! New price: $%.2f", 
                                      flightNumber, discountPercentage, newPrice);
        for (Observer o : observers) {
            o.update(message);
        }
    }
    
    public void removeSpecialOffer() {
        this.price = this.originalPrice;
        this.discountPercentage = 0;
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
        String message = String.format("Flight %s (%s to %s on %s) status updated to: %s",
                                       flightNumber, departure, arrival, departureDate, status);
        for (Observer o : observers) {
            o.update(message);
        }
    }

    public String getFlightNumber() { return flightNumber; }
    public String getDeparture() { return departure; }
    public String getArrival() { return arrival; }
    public String getAirline() { return airline; }
    public LocalDate getDepartureDate() { return departureDate; }
    public LocalTime getDepartureTime() { return departureTime; }
    public double getDuration() { return duration; }
    public double getPrice() { return price; }
    public double getOriginalPrice() { return originalPrice; }
    public int getDiscountPercentage() { return discountPercentage; }
    public String getStatus() { return status; }


    @Override
    public String toString() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String priceDisplay = discountPercentage > 0 
            ? String.format("Price: $%.2f (SPECIAL OFFER: %d%% OFF, was $%.2f)", 
                            price, discountPercentage, originalPrice)
            : String.format("Price: $%.2f", price);
            
        return String.format("Flight %s (%s) %s -> %s on %s at %s (%s hrs) - Status: %s - %s",
                             flightNumber, airline, departure, arrival, departureDate,
                             departureTime.format(timeFormatter), duration, status, priceDisplay);
    }
}