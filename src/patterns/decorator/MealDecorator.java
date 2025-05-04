package patterns.decorator;

import core.BookingComponent;

public class MealDecorator extends ServiceDecorator {
    private static final long serialVersionUID = 1L;
    
    public MealDecorator(BookingComponent booking) {
        super(booking);
    }

    @Override
    public double getCost() {
        return booking.getCost() + 20.0;
    }

    @Override
    public String getDescription() {
        return booking.getDescription() + ", with Meal";
    }
}