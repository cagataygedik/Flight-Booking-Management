package core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GroupBooking implements Serializable {
    private static final long serialVersionUID = 1L;
    private String groupId;
    private Flight flight;
    private List<Passenger> passengers;
    private List<Booking> bookings;
    private double totalCost;
    private double groupDiscountPercentage;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private Map<Passenger, String> seatAssignments;
    
    public GroupBooking(String groupId, Flight flight, List<Passenger> passengers, 
                      double groupDiscountPercentage, String contactName, 
                      String contactEmail, String contactPhone) {
        this.groupId = groupId;
        this.flight = flight;
        this.passengers = new ArrayList<>(passengers);
        this.bookings = new ArrayList<>();
        this.groupDiscountPercentage = groupDiscountPercentage;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.seatAssignments = new HashMap<>();
        
        
        
        createBookingsWithoutSeats();
        
        
        calculateTotalCost();
    }
    
    private void createBookingsWithoutSeats() {
        for (Passenger passenger : passengers) {
            
            BookingComponent basicBooking = new ConcreteBooking(flight, passenger.name, null);
            Booking booking = new Booking(basicBooking);
            bookings.add(booking);
        }
    }
    
    public boolean assignSeat(Passenger passenger, String seatCode) {
        int index = passengers.indexOf(passenger);
        if (index == -1) {
            return false; 
        }
        
        
        if (!flight.getSeatMap().isSeatAvailable(seatCode)) {
            return false; 
        }
        
        
        Booking booking = bookings.get(index);
        
        
        BookingComponent oldComponent = booking.bookingComponent;
        if (oldComponent instanceof ConcreteBooking) {
            ConcreteBooking concreteBooking = (ConcreteBooking) oldComponent;
            
            
            String oldSeat = concreteBooking.getSeatCode();
            if (oldSeat != null && !oldSeat.isEmpty()) {
                flight.getSeatMap().releaseSeat(oldSeat);
            }
            
            
            BookingComponent newBooking = new ConcreteBooking(flight, passenger.name, seatCode);
            booking.setBookingComponent(newBooking);
            
            
            seatAssignments.put(passenger, seatCode);
            
            return true;
        }
        
        return false;
    }
    
    public String getAssignedSeat(Passenger passenger) {
        return seatAssignments.get(passenger);
    }
    
    public Map<Passenger, String> getAllSeatAssignments() {
        return new HashMap<>(seatAssignments);
    }
    
    public boolean areAllSeatsAssigned() {
        return seatAssignments.size() == passengers.size();
    }
    
    private void calculateTotalCost() {
        double subtotal = 0;
        for (Booking booking : bookings) {
            subtotal += booking.getCost();
        }
        
        
        this.totalCost = subtotal * (1 - (groupDiscountPercentage / 100.0));
    }
    
    public boolean addPassenger(Passenger passenger) {
        if (!passengers.contains(passenger)) {
            passengers.add(passenger);
            
            
            BookingComponent basicBooking = new ConcreteBooking(flight, passenger.name, null);
            Booking booking = new Booking(basicBooking);
            bookings.add(booking);
            
            
            calculateTotalCost();
            return true;
        }
        return false;
    }
    
    public boolean removePassenger(Passenger passenger) {
        int index = passengers.indexOf(passenger);
        if (index != -1) {
            
            String seatCode = seatAssignments.get(passenger);
            if (seatCode != null) {
                flight.getSeatMap().releaseSeat(seatCode);
                seatAssignments.remove(passenger);
            }
            
            passengers.remove(index);
            bookings.remove(index);
            
            
            calculateTotalCost();
            return true;
        }
        return false;
    }
    
    
    
    public String getGroupId() {
        return groupId;
    }
    
    public Flight getFlight() {
        return flight;
    }
    
    public List<Passenger> getPassengers() {
        return new ArrayList<>(passengers);
    }
    
    public List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }
    
    public double getTotalCost() {
        return totalCost;
    }
    
    public double getGroupDiscountPercentage() {
        return groupDiscountPercentage;
    }
    
    public void setGroupDiscountPercentage(double groupDiscountPercentage) {
        this.groupDiscountPercentage = groupDiscountPercentage;
        calculateTotalCost();
    }
    
    public String getContactName() {
        return contactName;
    }
    
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    public int getPassengerCount() {
        return passengers.size();
    }
    
    @Override
    public String toString() {
        return String.format("Group Booking %s: %s passengers for flight %s (%s to %s) - Group Discount: %.1f%% - Total Cost: $%.2f",
                            groupId, passengers.size(), flight.getFlightNumber(), 
                            flight.getDeparture(), flight.getArrival(), 
                            groupDiscountPercentage, totalCost);
    }
} 