abstract class ServiceDecorator implements BookingComponent {
    protected BookingComponent booking;

    public ServiceDecorator(BookingComponent booking) {
        this.booking = booking;
    }
}
