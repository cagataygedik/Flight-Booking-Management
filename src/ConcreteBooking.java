class ConcreteBooking implements BookingComponent {
    private Flight flight;
    private String passengerName;
    private String seat;

    public ConcreteBooking(Flight flight, String passengerName, String seat) {
        this.flight = flight;
        this.passengerName = passengerName;
        this.seat = seat;
    }

    @Override
    public double getCost() {
        return flight.getPrice();
    }

    @Override
    public String getDescription() {
        return String.format("Flight %s (%s -> %s) booking for %s (Seat: %s)",
                             flight.getFlightNumber(), flight.getDeparture(), flight.getArrival(),
                             passengerName, seat);
    }

    @Override
    public Flight getFlight() {
        return flight;
    }
}

    // Add getFlight() to the interface and implement here
    // (As per previous recommendation for Liskov Substitution)
    // @Override
    // public Flight getFlight() {
    //     return flight;
    // }
