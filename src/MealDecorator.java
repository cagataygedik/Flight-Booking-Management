class MealDecorator extends ServiceDecorator {
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