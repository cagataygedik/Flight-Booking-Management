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
        return 200.0; // Base cost
    }

    @Override
    public String getDescription() {
        return "Flight " + flight.getFlightNumber() + " booking for " + passengerName + " (Seat: " + seat + ")";
    }

    public Flight getFlight() {
        return flight;
    }
}
