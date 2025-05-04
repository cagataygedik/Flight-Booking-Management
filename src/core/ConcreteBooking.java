package core;

import java.io.Serializable;

public class ConcreteBooking implements BookingComponent {
    private static final long serialVersionUID = 1L;
    private Flight flight;
    private String passengerName;
    private String seatCode;

    public ConcreteBooking(Flight flight, String passengerName, String seatCode) {
        this.flight = flight;
        this.passengerName = passengerName;
        this.seatCode = seatCode;
        
        
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
    
    public String getPassengerName() {
        return passengerName;
    }
    
    public String getSeatCode() {
        return seatCode;
    }
    
    public boolean changeSeat(String newSeatCode) {
        
        if (seatCode != null && !seatCode.isEmpty()) {
            flight.getSeatMap().releaseSeat(seatCode);
        }
        
        
        if (flight.getSeatMap().bookSeat(newSeatCode)) {
            this.seatCode = newSeatCode;
            return true;
        } else {
            
            if (seatCode != null && !seatCode.isEmpty()) {
                flight.getSeatMap().bookSeat(seatCode);
            }
            return false;
        }
    }
}

    
    
    
    
    
    
