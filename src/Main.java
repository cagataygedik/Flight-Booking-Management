import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static FlightDatabase flightDb = new FlightDatabase();
    private static Scanner scanner = new Scanner(System.in);
    private static Passenger currentPassenger;

    public static void main(String[] args) {
        System.out.println(ConsoleColors.CYAN + "Welcome to the Flight Booking Management System!" + ConsoleColors.RESET);
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        currentPassenger = new Passenger(name);

        while (true) {
            displayMenu();
            int choice = getNumericChoice(1,8);
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
                    viewBookings();
                    break;
                case 5:
                    cancelBooking();
                    break;
                case 6:
                    checkFlightStatus();
                    break;
                case 7:
                    viewLoyaltyPoints();
                    break;
                case 8:
                    System.out.println(ConsoleColors.GREEN + "Thank you for using the system!" + ConsoleColors.RESET);
                    return;
                default:
                    System.out.println(ConsoleColors.RED + "Invalid choice. Please try again." + ConsoleColors.RESET);
            }
        }
    }

    private static void displayMenu() {
        System.out.println(ConsoleColors.CYAN + "\n--- Menu ---" + ConsoleColors.RESET);
        System.out.println("1. Search for Flights");
        System.out.println("2. Book a Flight");
        System.out.println("3. Customize Booking");
        System.out.println("4. View My Bookings");
        System.out.println("5. Cancel a Booking");
        System.out.println("6. Check Flight Status");
        System.out.println("7. View Loyalty Points");
        System.out.println("8. Exit");
        System.out.print("Choose an option: ");
    }

    private static int getNumericChoice(int min, int max) {
        while (true) {
            try {
                System.out.print("Enter your choice (" + min + "-" + max + "): ");
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    System.out.println(ConsoleColors.RED + "Please enter a number between " + min + " and " + max + "." + ConsoleColors.RESET);
                }
            } catch (InputMismatchException e) {
                System.out.println(ConsoleColors.RED + "Invalid input. Please enter a number." + ConsoleColors.RESET);
                scanner.nextLine();
            }
        }
    }

    private static void displayStringList(List<String> list, String title) {
        System.out.println(ConsoleColors.CYAN + "--- " + title + " ---" + ConsoleColors.RESET);
        if (list.isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "No options available." + ConsoleColors.RESET);
            return;
        }
        System.out.println(String.join(", ", list));
    }

    private static void searchFlights() {
        List<String> departures = flightDb.getAvailableDepartures();
        if (departures.isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "No departure cities found in the database." + ConsoleColors.RESET);
            return;
        }
        displayStringList(departures, "Available Departure Cities");
        String selectedDeparture = null;
        while (selectedDeparture == null) {
            System.out.print("Enter departure city code: ");
            String input = scanner.nextLine().toUpperCase();
            for (String city : departures) {
                if (city.equalsIgnoreCase(input)) {
                    selectedDeparture = city;
                    break;
                }
            }
            if (selectedDeparture == null) {
                System.out.println(ConsoleColors.RED + "Invalid departure city code. Please choose from the list above." + ConsoleColors.RESET);
            }
        }

        List<String> arrivals = flightDb.getAvailableArrivalsFrom(selectedDeparture);
        if (arrivals.isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "No arrival cities found from " + selectedDeparture + "." + ConsoleColors.RESET);
            return;
        }
        displayStringList(arrivals, "Available Arrival Cities from " + selectedDeparture);
        String selectedArrival = null;
        while (selectedArrival == null) {
            System.out.print("Enter arrival city code: ");
            String input = scanner.nextLine().toUpperCase();
            for (String city : arrivals) {
                if (city.equalsIgnoreCase(input)) {
                    selectedArrival = city;
                    break;
                }
            }
            if (selectedArrival == null) {
                System.out.println(ConsoleColors.RED + "Invalid arrival city code. Please choose from the list above." + ConsoleColors.RESET);
            }
        }

        System.out.println(ConsoleColors.CYAN + "\nSearching for flights from " + selectedDeparture + " to " + selectedArrival + "..." + ConsoleColors.RESET);
        List<Flight> results = flightDb.searchFlights(selectedDeparture, selectedArrival);
        if (results.isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "No flights found for this route." + ConsoleColors.RESET);
        } else {
            System.out.println(ConsoleColors.CYAN + "Available flights:" + ConsoleColors.RESET);
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
            System.out.println(ConsoleColors.RED + "Flight not found." + ConsoleColors.RESET);
            return;
        }
        System.out.print("Select seat (window, aisle, middle): ");
        String seat = scanner.nextLine();
        BookingComponent bookingComponent = new ConcreteBooking(flight, currentPassenger.name, seat);
        Booking booking = new Booking(bookingComponent);
        currentPassenger.addBooking(booking);
        currentPassenger.subscribeToFlight(flight);
        System.out.println(ConsoleColors.GREEN + "Booking created: " + booking.getDescription() + " - Cost: $" + booking.getCost() + ConsoleColors.RESET);
        System.out.println(ConsoleColors.GREEN + "You are now subscribed to updates for Flight " + flightNumber + ConsoleColors.RESET);
    }

    private static void customizeBooking() {
        if (currentPassenger.getBookings().isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "You have no bookings to customize." + ConsoleColors.RESET);
            return;
        }
        System.out.println(ConsoleColors.CYAN + "Select a booking to customize:" + ConsoleColors.RESET);
        for (int i = 0; i < currentPassenger.getBookings().size(); i++) {
            System.out.println((i + 1) + ". " + currentPassenger.getBookings().get(i).getDescription());
        }
        System.out.print("Enter booking number: ");
        int bookingIndex = scanner.nextInt() - 1;
        scanner.nextLine();
        if (bookingIndex < 0 || bookingIndex >= currentPassenger.getBookings().size()) {
            System.out.println(ConsoleColors.RED + "Invalid booking number." + ConsoleColors.RESET);
            return;
        }
        Booking booking = currentPassenger.getBookings().get(bookingIndex);
        BookingComponent bookingComponent = booking.bookingComponent;
        System.out.println(ConsoleColors.CYAN + "Customize your booking with additional services:" + ConsoleColors.RESET);
        while (true) {
            System.out.println("1. Add Insurance (+$50)");
            System.out.println("2. Add Meal (+$20)");
            System.out.println("3. Add Priority Boarding (+$30)");
            System.out.println("4. Done");
            System.out.print("Choose a service to add (or 4 to finish): ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 4) break;
            switch (choice) {
                case 1:
                    bookingComponent = new InsuranceDecorator(bookingComponent);
                    break;
                case 2:
                    bookingComponent = new MealDecorator(bookingComponent);
                    break;
                case 3:
                    bookingComponent = new PriorityBoardingDecorator(bookingComponent);
                    break;
                default:
                    System.out.println(ConsoleColors.RED + "Invalid choice." + ConsoleColors.RESET);
            }
        }
        booking.bookingComponent = bookingComponent;
        System.out.println(ConsoleColors.GREEN + "Final Booking: " + booking.getDescription() + " - Total Cost: $" + booking.getCost() + ConsoleColors.RESET);
    }

    private static void viewBookings() {
        if (currentPassenger.getBookings().isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "You have no bookings." + ConsoleColors.RESET);
        } else {
            System.out.println(ConsoleColors.CYAN + "Your Bookings:" + ConsoleColors.RESET);
            for (Booking booking : currentPassenger.getBookings()) {
                System.out.println(booking.getDescription() + " - Cost: $" + booking.getCost());
            }
        }
    }

    private static void cancelBooking() {
        if (currentPassenger.getBookings().isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "You have no bookings to cancel." + ConsoleColors.RESET);
            return;
        }
        System.out.println(ConsoleColors.CYAN + "Select a booking to cancel:" + ConsoleColors.RESET);
        for (int i = 0; i < currentPassenger.getBookings().size(); i++) {
            System.out.println((i + 1) + ". " + currentPassenger.getBookings().get(i).getDescription());
        }
        System.out.print("Enter booking number: ");
        int bookingIndex = scanner.nextInt() - 1;
        scanner.nextLine();
        if (bookingIndex < 0 || bookingIndex >= currentPassenger.getBookings().size()) {
            System.out.println(ConsoleColors.RED + "Invalid booking number." + ConsoleColors.RESET);
            return;
        }
        Booking booking = currentPassenger.getBookings().get(bookingIndex);
        currentPassenger.cancelBooking(booking);
        System.out.println(ConsoleColors.GREEN + "Booking canceled successfully." + ConsoleColors.RESET);
    }

    private static void checkFlightStatus() {
        System.out.print("Enter flight number to check status: ");
        String flightNumber = scanner.nextLine();
        Flight flight = flightDb.getFlightByNumber(flightNumber);
        if (flight == null) {
            System.out.println(ConsoleColors.RED + "Flight not found." + ConsoleColors.RESET);
        } else {
            System.out.println(flight);
        }
    }

    private static void viewLoyaltyPoints() {
        System.out.println(ConsoleColors.CYAN + "Your Loyalty Points: " + currentPassenger.getLoyaltyPoints() + ConsoleColors.RESET);
    }
}
