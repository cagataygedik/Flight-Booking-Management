package patterns.decorator;

import core.BookingComponent;

public class PriorityBoardingDecorator extends ServiceDecorator {
    private static final long serialVersionUID = 1L;
    
    public PriorityBoardingDecorator(BookingComponent booking) {
        super(booking);
    }

    @Override
    public double getCost() {
        return booking.getCost() + 30.0;
    }

    @Override
    public String getDescription() {
        return booking.getDescription() + ", with Priority Boarding";
    }
}
