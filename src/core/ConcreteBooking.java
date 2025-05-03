package core;

public class ConcreteBooking implements BookingComponent {
    private Flight flight;
    private String passengerName;
    private String seatCode;

    public ConcreteBooking(Flight flight, String passengerName, String seatCode) {
        this.flight = flight;
        this.passengerName = passengerName;
        this.seatCode = seatCode;
        
        // Book the seat in the seat map
        if (seatCode != null && !seatCode.isEmpty()) {
            flight.getSeatMap().bookSeat(seatCode);
        }
    }

    @Override
    public double getCost() {
        return flight.getPrice();
    }

    @Override
    public String getDescription() {
        return String.format("Flight %s (%s -> %s) booking for %s (Seat: %s)",
                             flight.getFlightNumber(), flight.getDeparture(), flight.getArrival(),
                             passengerName, seatCode);
    }

    @Override
    public Flight getFlight() {
        return flight;
    }
    
    /**
     * Gets the passenger name.
     * @return Passenger name
     */
    public String getPassengerName() {
        return passengerName;
    }
    
    /**
     * Gets the seat code.
     * @return Seat code
     */
    public String getSeatCode() {
        return seatCode;
    }
    
    /**
     * Change the seat for this booking.
     * @param newSeatCode The new seat code
     * @return true if successful, false otherwise
     */
    public boolean changeSeat(String newSeatCode) {
        // Release the old seat
        if (seatCode != null && !seatCode.isEmpty()) {
            flight.getSeatMap().releaseSeat(seatCode);
        }
        
        // Book the new seat
        if (flight.getSeatMap().bookSeat(newSeatCode)) {
            this.seatCode = newSeatCode;
            return true;
        } else {
            // If booking the new seat failed, re-book the old seat
            if (seatCode != null && !seatCode.isEmpty()) {
                flight.getSeatMap().bookSeat(seatCode);
            }
            return false;
        }
    }
}

    // Add getFlight() to the interface and implement here
    // (As per previous recommendation for Liskov Substitution)
    // @Override
    // public Flight getFlight() {
    //     return flight;
    // }
