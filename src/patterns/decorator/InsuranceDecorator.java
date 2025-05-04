package patterns.decorator;

import core.BookingComponent;

public class InsuranceDecorator extends ServiceDecorator {
    private static final long serialVersionUID = 1L;
    
    public InsuranceDecorator(BookingComponent booking) {
        super(booking);
    }

    @Override
    public double getCost() {
        return booking.getCost() + 50.0;
    }

    @Override
    public String getDescription() {
        return booking.getDescription() + ", with Insurance";
    }
}