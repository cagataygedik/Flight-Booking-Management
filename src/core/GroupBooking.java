package core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a group booking that contains multiple passengers for the same flight.
 * Implements the Serializable interface for data persistence.
 */
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
    
    /**
     * Constructor for creating a new group booking.
     * 
     * @param groupId Unique identifier for the group
     * @param flight The flight being booked
     * @param passengers List of passengers in the group
     * @param groupDiscountPercentage Discount percentage for group booking
     * @param contactName Name of the primary contact person
     * @param contactEmail Email of the primary contact person
     * @param contactPhone Phone number of the primary contact person
     */
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
        
        // Initially create bookings without seat assignments
        // Seats will be assigned in a separate step
        createBookingsWithoutSeats();
        
        // Calculate total cost with group discount
        calculateTotalCost();
    }
    
    /**
     * Creates initial bookings for each passenger without seat assignments.
     * Seats will be assigned later in the process.
     */
    private void createBookingsWithoutSeats() {
        for (Passenger passenger : passengers) {
            // Create booking without seat assignment
            BookingComponent basicBooking = new ConcreteBooking(flight, passenger.name, null);
            Booking booking = new Booking(basicBooking);
            bookings.add(booking);
        }
    }
    
    /**
     * Assigns a specific seat to a passenger in the group.
     * 
     * @param passenger The passenger to assign the seat to
     * @param seatCode The seat code to assign
     * @return true if seat assignment was successful
     */
    public boolean assignSeat(Passenger passenger, String seatCode) {
        int index = passengers.indexOf(passenger);
        if (index == -1) {
            return false; // Passenger not in this group
        }
        
        // Check if seat is available
        if (!flight.getSeatMap().isSeatAvailable(seatCode)) {
            return false; // Seat not available
        }
        
        // Get the corresponding booking
        Booking booking = bookings.get(index);
        
        // Update the booking with the new seat
        BookingComponent oldComponent = booking.bookingComponent;
        if (oldComponent instanceof ConcreteBooking) {
            ConcreteBooking concreteBooking = (ConcreteBooking) oldComponent;
            
            // If there was a previous seat, release it
            String oldSeat = concreteBooking.getSeatCode();
            if (oldSeat != null && !oldSeat.isEmpty()) {
                flight.getSeatMap().releaseSeat(oldSeat);
            }
            
            // Create new booking with the seat and update
            BookingComponent newBooking = new ConcreteBooking(flight, passenger.name, seatCode);
            booking.setBookingComponent(newBooking);
            
            // Store the seat assignment
            seatAssignments.put(passenger, seatCode);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Get the seat assigned to a specific passenger.
     * 
     * @param passenger The passenger to check
     * @return The assigned seat code or null if no seat is assigned
     */
    public String getAssignedSeat(Passenger passenger) {
        return seatAssignments.get(passenger);
    }
    
    /**
     * Get a map of all seat assignments for this group.
     * 
     * @return Map of passengers to their assigned seats
     */
    public Map<Passenger, String> getAllSeatAssignments() {
        return new HashMap<>(seatAssignments);
    }
    
    /**
     * Check if all passengers have seat assignments.
     * 
     * @return true if all passengers have assigned seats
     */
    public boolean areAllSeatsAssigned() {
        return seatAssignments.size() == passengers.size();
    }
    
    /**
     * Calculates the total cost of all bookings with the group discount applied.
     */
    private void calculateTotalCost() {
        double subtotal = 0;
        for (Booking booking : bookings) {
            subtotal += booking.getCost();
        }
        
        // Apply group discount
        this.totalCost = subtotal * (1 - (groupDiscountPercentage / 100.0));
    }
    
    /**
     * Adds a passenger to the group booking.
     * 
     * @param passenger The passenger to add
     * @return true if passenger was added successfully
     */
    public boolean addPassenger(Passenger passenger) {
        if (!passengers.contains(passenger)) {
            passengers.add(passenger);
            
            // Create booking for new passenger without seat assignment
            BookingComponent basicBooking = new ConcreteBooking(flight, passenger.name, null);
            Booking booking = new Booking(basicBooking);
            bookings.add(booking);
            
            // Recalculate total cost
            calculateTotalCost();
            return true;
        }
        return false;
    }
    
    /**
     * Removes a passenger from the group booking.
     * 
     * @param passenger The passenger to remove
     * @return true if passenger was removed successfully
     */
    public boolean removePassenger(Passenger passenger) {
        int index = passengers.indexOf(passenger);
        if (index != -1) {
            // Release the seat if one was assigned
            String seatCode = seatAssignments.get(passenger);
            if (seatCode != null) {
                flight.getSeatMap().releaseSeat(seatCode);
                seatAssignments.remove(passenger);
            }
            
            passengers.remove(index);
            bookings.remove(index);
            
            // Recalculate total cost
            calculateTotalCost();
            return true;
        }
        return false;
    }
    
    // Getters and setters
    
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
    
    /**
     * Returns a string representation of the group booking.
     */
    @Override
    public String toString() {
        return String.format("Group Booking %s: %s passengers for flight %s (%s to %s) - Group Discount: %.1f%% - Total Cost: $%.2f",
                            groupId, passengers.size(), flight.getFlightNumber(), 
                            flight.getDeparture(), flight.getArrival(), 
                            groupDiscountPercentage, totalCost);
    }
} 