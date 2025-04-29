class Booking {
    BookingComponent bookingComponent;

    public Booking(BookingComponent bookingComponent) {
        this.bookingComponent = bookingComponent;
    }

    public double getCost() {
        return bookingComponent.getCost();
    }

    public String getDescription() {
        return bookingComponent.getDescription();
    }

    public Flight getFlight() {
        return bookingComponent.getFlight();
    }
}