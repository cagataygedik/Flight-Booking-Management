package patterns.decorator;

import core.BookingComponent;
import core.Flight;

public abstract class ServiceDecorator implements BookingComponent {
    private static final long serialVersionUID = 1L;
    protected BookingComponent booking;

    public ServiceDecorator(BookingComponent booking) {
        this.booking = booking;
    }

    @Override
    public Flight getFlight() {
        return booking.getFlight();
    }
}