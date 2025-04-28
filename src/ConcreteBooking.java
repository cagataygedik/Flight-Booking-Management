class ConcreteBooking implements BookingComponent {
    private Flight flight;
    private String passengerName;

    public ConcreteBooking(Flight flight, String passengerName) {
        this.flight = flight;
        this.passengerName = passengerName;
    }

    @Override
    public double getCost() {
        return 200.0; // Base cost
    }

    @Override
    public String getDescription() {
        return "Flight " + flight.getFlightNumber() + " booking for " + passengerName;
    }
}
