import java.util.List;
import java.util.Scanner;

public class Main {
    private static FlightDatabase flightDb = new FlightDatabase();
    private static Scanner scanner = new Scanner(System.in);
    private static Passenger currentPassenger;

    public static void main(String[] args) {
        System.out.println("Welcome to the Flight Booking Management System!");
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        currentPassenger = new Passenger(name);

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Search for Flights");
            System.out.println("2. Book a Flight");
            System.out.println("3. Customize Booking");
            System.out.println("4. Check Flight Status");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    searchFlights();
                    break;
                case 2:
                    bookFlight();
                    break;
                case 3:
                    customizeBooking();
                    break;
                case 4:
                    checkFlightStatus();
                    break;
                case 5:
                    System.out.println("Thank you for using the system!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void searchFlights() {
        System.out.print("Enter departure city: ");
        String departure = scanner.nextLine();
        System.out.print("Enter arrival city: ");
        String arrival = scanner.nextLine();
        List<Flight> results = flightDb.searchFlights(departure, arrival);
        if (results.isEmpty()) {
            System.out.println("No flights found.");
        } else {
            System.out.println("Available flights:");
            for (Flight flight : results) {
                System.out.println(flight);
            }
        }
    }

    private static void bookFlight() {
        System.out.print("Enter flight number to book: ");
        String flightNumber = scanner.nextLine();
        Flight flight = flightDb.getFlightByNumber(flightNumber);
        if (flight == null) {
            System.out.println("Flight not found.");
            return;
        }
        BookingComponent booking = new ConcreteBooking(flight, currentPassenger.name);
        System.out.println("Booking created: " + booking.getDescription() + " - Cost: $" + booking.getCost());
        currentPassenger.subscribeToFlight(flight);
        System.out.println("You are now subscribed to updates for Flight " + flightNumber);
    }

    private static void customizeBooking() {
        // For simplicity, assume the user has only one booking
        System.out.println("Customize your booking with additional services:");
        System.out.println("1. Add Insurance (+$50)");
        System.out.println("2. Add Meal (+$20)");
        System.out.println("3. Add Priority Boarding (+$30)");
        System.out.println("4. Done");
        BookingComponent booking = new ConcreteBooking(flightDb.getFlightByNumber("FL123"), currentPassenger.name); // Placeholder
        while (true) {
            System.out.print("Choose a service to add (or 4 to finish): ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            if (choice == 4) break;
            switch (choice) {
                case 1:
                    booking = new InsuranceDecorator(booking);
                    break;
                case 2:
                    booking = new MealDecorator(booking);
                    break;
                case 3:
                    booking = new PriorityBoardingDecorator(booking);
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
        System.out.println("Final Booking: " + booking.getDescription() + " - Total Cost: $" + booking.getCost());
    }

    private static void checkFlightStatus() {
        System.out.print("Enter flight number to check status: ");
        String flightNumber = scanner.nextLine();
        Flight flight = flightDb.getFlightByNumber(flightNumber);
        if (flight == null) {
            System.out.println("Flight not found.");
        } else {
            System.out.println(flight);
        }
    }
}
