package ui;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import auth.User;
import core.Booking;
import core.BookingComponent;
import core.ConcreteBooking;
import core.Flight;
import core.Passenger;
import data.FlightDatabase;
import payment.PaymentProcessor;
import patterns.decorator.InsuranceDecorator;
import patterns.decorator.MealDecorator;
import patterns.decorator.PriorityBoardingDecorator;

public class FlightBookingApp {
    private static FlightDatabase flightDb = new FlightDatabase();
    private static Scanner scanner = new Scanner(System.in);
    private static Passenger currentPassenger;
    private static User currentUser;
    private static PaymentProcessor paymentProcessor = new PaymentProcessor(scanner);

    public static void main(String[] args) {
        System.out.println(ConsoleColors.CYAN + "Welcome to the Flight Booking Management System!" + ConsoleColors.RESET);
        
        // Start with login/register menu
        while (currentUser == null) {
            showLoginMenu();
        }
        
        currentPassenger = currentUser.getPassenger();
        System.out.println(ConsoleColors.GREEN + "Welcome, " + currentPassenger.name + "!" + ConsoleColors.RESET);

        while (true) {
            displayMenu();
            int choice = getNumericChoice(1, currentUser.isAdmin() ? 10 : 9);
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
                    selectChangeSeats();
                    break;
                case 9:
                    if (currentUser.isAdmin()) {
                        showAdminPanel();
                    } else {
                        System.out.println(ConsoleColors.GREEN + "Thank you for using the system, " + 
                                           currentUser.getUsername() + "!" + ConsoleColors.RESET);
                        return;
                    }
                    break;
                case 10: // Only available for admin users
                    System.out.println(ConsoleColors.GREEN + "Thank you for using the system, " + 
                                       currentUser.getUsername() + "!" + ConsoleColors.RESET);
                    return;
                default:
                    System.out.println(ConsoleColors.RED + "Invalid choice. Please try again." + ConsoleColors.RESET);
            }
        }
    }
    
    private static void showLoginMenu() {
        System.out.println(ConsoleColors.CYAN + "\n--- Login/Register ---" + ConsoleColors.RESET);
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        
        int choice = getNumericChoice(1, 3);
        
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 3:
                System.out.println(ConsoleColors.GREEN + "Thank you for visiting!" + ConsoleColors.RESET);
                System.exit(0);
                break;
            default:
                System.out.println(ConsoleColors.RED + "Invalid choice. Please try again." + ConsoleColors.RESET);
        }
    }
    
    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        currentUser = User.authenticate(username, password);
        
        if (currentUser == null) {
            System.out.println(ConsoleColors.RED + "Invalid username or password." + ConsoleColors.RESET);
        }
    }
    
    private static void register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter your full name: ");
        String fullName = scanner.nextLine();
        
        try {
            User.register(username, password, fullName);
            System.out.println(ConsoleColors.GREEN + "Registration successful! You can now login." + ConsoleColors.RESET);
        } catch (IllegalArgumentException e) {
            System.out.println(ConsoleColors.RED + e.getMessage() + ConsoleColors.RESET);
        }
    }
    
    private static void showAdminPanel() {
        AdminPanel adminPanel = new AdminPanel(flightDb, scanner);
        adminPanel.run();
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
        System.out.println("8. Select/Change Seats");
        
        if (currentUser.isAdmin()) {
            System.out.println("9. Admin Panel");
            System.out.println("10. Exit");
        } else {
            System.out.println("9. Exit");
        }
        
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
        // Define search steps for our state machine
        enum SearchStep {
            SELECT_DEPARTURE,
            SELECT_ARRIVAL,
            APPLY_FILTERS,
            SORT_RESULTS,
            DISPLAY_RESULTS,
            EXIT
        }
        
        SearchStep currentStep = SearchStep.SELECT_DEPARTURE;
        String selectedDeparture = null;
        String selectedArrival = null;
        List<Flight> results = null;
        
        while (currentStep != SearchStep.EXIT) {
            switch (currentStep) {
                case SELECT_DEPARTURE:
                    List<String> departures = flightDb.getAvailableDepartures();
                    if (departures.isEmpty()) {
                        System.out.println(ConsoleColors.YELLOW + "No departure cities found in the database." + ConsoleColors.RESET);
                        currentStep = SearchStep.EXIT;
                        break;
                    }
                    
                    displayStringList(departures, "Available Departure Cities");
                    System.out.println("Enter 'back' to return to main menu");
                    System.out.print("Enter departure city code: ");
                    String departureInput = scanner.nextLine().toUpperCase();
                    
                    if (departureInput.equalsIgnoreCase("back")) {
                        currentStep = SearchStep.EXIT;
                        break;
                    }
                    
                    boolean validDeparture = false;
                    for (String city : departures) {
                        if (city.equalsIgnoreCase(departureInput)) {
                            selectedDeparture = city;
                            validDeparture = true;
                            break;
                        }
                    }
                    
                    if (validDeparture) {
                        currentStep = SearchStep.SELECT_ARRIVAL;
                    } else {
                        System.out.println(ConsoleColors.RED + "Invalid departure city code. Please try again." + ConsoleColors.RESET);
                    }
                    break;
                    
                case SELECT_ARRIVAL:
                    List<String> arrivals = flightDb.getAvailableArrivalsFrom(selectedDeparture);
                    if (arrivals.isEmpty()) {
                        System.out.println(ConsoleColors.YELLOW + "No arrival cities found from " + selectedDeparture + "." + ConsoleColors.RESET);
                        currentStep = SearchStep.SELECT_DEPARTURE;
                        break;
                    }
                    
                    displayStringList(arrivals, "Available Arrival Cities from " + selectedDeparture);
                    System.out.println("Enter 'back' to select a different departure city");
                    System.out.print("Enter arrival city code: ");
                    String arrivalInput = scanner.nextLine().toUpperCase();
                    
                    if (arrivalInput.equalsIgnoreCase("back")) {
                        currentStep = SearchStep.SELECT_DEPARTURE;
                        break;
                    }
                    
                    boolean validArrival = false;
                    for (String city : arrivals) {
                        if (city.equalsIgnoreCase(arrivalInput)) {
                            selectedArrival = city;
                            validArrival = true;
                            break;
                        }
                    }
                    
                    if (validArrival) {
                        System.out.println(ConsoleColors.CYAN + "\nSearching for flights from " + selectedDeparture + 
                                           " to " + selectedArrival + "..." + ConsoleColors.RESET);
                        results = flightDb.searchFlights(selectedDeparture, selectedArrival);
                        
                        if (results.isEmpty()) {
                            System.out.println(ConsoleColors.YELLOW + "No flights found for this route." + ConsoleColors.RESET);
                            System.out.println("Enter 'back' to select different cities or any key to return to main menu");
                            String noResultsChoice = scanner.nextLine();
                            currentStep = noResultsChoice.equalsIgnoreCase("back") ? 
                                          SearchStep.SELECT_ARRIVAL : SearchStep.EXIT;
                        } else {
                            currentStep = SearchStep.APPLY_FILTERS;
                        }
                    } else {
                        System.out.println(ConsoleColors.RED + "Invalid arrival city code. Please try again." + ConsoleColors.RESET);
                    }
                    break;
                    
                case APPLY_FILTERS:
                    System.out.println(ConsoleColors.CYAN + "\n--- Apply Filters (Optional) ---" + ConsoleColors.RESET);
                    System.out.println("Would you like to filter the results?");
                    System.out.println("1. Filter by date range");
                    System.out.println("2. Filter by maximum price");
                    System.out.println("3. Filter by airline");
                    System.out.println("4. Filter by maximum duration");
                    System.out.println("5. No filters");
                    System.out.println("6. Back to arrival selection");
                    System.out.print("Enter your choice (1-6): ");
                    
                    int filterChoice;
                    try {
                        filterChoice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        filterChoice = 5; // Default to no filters
                    }
                    
                    if (filterChoice == 6) {
                        currentStep = SearchStep.SELECT_ARRIVAL;
                        break;
                    }
                    
                    List<Flight> filteredResults = new ArrayList<>(results);
                    
                    switch (filterChoice) {
                        case 1: // Date range
                            filteredResults = filterByDateRange(filteredResults);
                            break;
                        case 2: // Max price
                            filteredResults = filterByMaxPrice(filteredResults);
                            break;
                        case 3: // Airline
                            filteredResults = filterByAirline(filteredResults);
                            break;
                        case 4: // Max duration
                            filteredResults = filterByMaxDuration(filteredResults);
                            break;
                        default:
                            System.out.println(ConsoleColors.YELLOW + "No filters applied." + ConsoleColors.RESET);
                    }
                    
                    if (filteredResults.isEmpty()) {
                        System.out.println(ConsoleColors.YELLOW + "No flights match your filters." + ConsoleColors.RESET);
                        System.out.println("Enter 'back' to try different filters or any key to start a new search");
                        String noMatchChoice = scanner.nextLine();
                        currentStep = noMatchChoice.equalsIgnoreCase("back") ? 
                                      SearchStep.APPLY_FILTERS : SearchStep.SELECT_DEPARTURE;
                        break;
                    }
                    
                    results = filteredResults;
                    currentStep = SearchStep.SORT_RESULTS;
                    break;
                    
                case SORT_RESULTS:
                    System.out.println(ConsoleColors.CYAN + "\n--- Sort Results ---" + ConsoleColors.RESET);
                    System.out.println("How would you like to sort the results?");
                    System.out.println("1. Price (lowest first)");
                    System.out.println("2. Duration (shortest first)");
                    System.out.println("3. Departure time (earliest first)");
                    System.out.println("4. Airline (alphabetically)");
                    System.out.println("5. No sorting");
                    System.out.println("6. Back to filters");
                    System.out.print("Enter your choice (1-6): ");
                    
                    int sortChoice;
                    try {
                        sortChoice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        sortChoice = 5; // Default to no sorting
                    }
                    
                    if (sortChoice == 6) {
                        currentStep = SearchStep.APPLY_FILTERS;
                        break;
                    }
                    
                    List<Flight> sortedResults = new ArrayList<>(results);
                    
                    switch (sortChoice) {
                        case 1: // Price
                            sortedResults.sort(Comparator.comparing(Flight::getPrice));
                            System.out.println(ConsoleColors.GREEN + "Sorted by price (lowest first)" + ConsoleColors.RESET);
                            break;
                        case 2: // Duration
                            sortedResults.sort(Comparator.comparing(Flight::getDuration));
                            System.out.println(ConsoleColors.GREEN + "Sorted by duration (shortest first)" + ConsoleColors.RESET);
                            break;
                        case 3: // Departure time
                            sortedResults.sort(Comparator.comparing(Flight::getDepartureDate)
                                            .thenComparing(Flight::getDepartureTime));
                            System.out.println(ConsoleColors.GREEN + "Sorted by departure time (earliest first)" + ConsoleColors.RESET);
                            break;
                        case 4: // Airline
                            sortedResults.sort(Comparator.comparing(Flight::getAirline));
                            System.out.println(ConsoleColors.GREEN + "Sorted by airline (alphabetically)" + ConsoleColors.RESET);
                            break;
                        default:
                            System.out.println(ConsoleColors.YELLOW + "No sorting applied." + ConsoleColors.RESET);
                    }
                    
                    results = sortedResults;
                    currentStep = SearchStep.DISPLAY_RESULTS;
                    break;
                    
                case DISPLAY_RESULTS:
                    displayFlightsTable(results);
                    System.out.println("\nEnter 'book' to book a flight, 'back' to modify search parameters, or any key to return to main menu");
                    String displayChoice = scanner.nextLine().toLowerCase();
                    
                    if (displayChoice.equals("book")) {
                        System.out.print("Enter flight number to book: ");
                        String flightNumber = scanner.nextLine();
                        Flight selectedFlight = null;
                        
                        for (Flight flight : results) {
                            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                                selectedFlight = flight;
                                break;
                            }
                        }
                        
                        if (selectedFlight != null) {
                            // Start booking process for the selected flight
                            bookSpecificFlight(selectedFlight);
                            currentStep = SearchStep.EXIT;
                        } else {
                            System.out.println(ConsoleColors.RED + "Invalid flight number. Please try again." + ConsoleColors.RESET);
                            // Stay on display results page
                        }
                    } else if (displayChoice.equals("back")) {
                        currentStep = SearchStep.SORT_RESULTS;
                    } else {
                        currentStep = SearchStep.EXIT;
                    }
                    break;
                    
                case EXIT:
                    // Exit the search flow and return to main menu
                    break;
            }
        }
    }
    
    /**
     * Helper method to book a specific flight selected from search results
     */
    private static void bookSpecificFlight(Flight flight) {
        System.out.println(ConsoleColors.CYAN + "Booking Flight: " + flight.getFlightNumber() + 
                           " from " + flight.getDeparture() + " to " + flight.getArrival() + ConsoleColors.RESET);
        
        // Use seat map visualizer to select a seat
        SeatMapVisualizer seatVisualizer = new SeatMapVisualizer(flight.getSeatMap());
        System.out.println(ConsoleColors.CYAN + "Please select your seat:" + ConsoleColors.RESET);
        String seatCode = seatVisualizer.selectSeat(scanner);
        
        if (seatCode == null) {
            System.out.println(ConsoleColors.YELLOW + "Booking cancelled." + ConsoleColors.RESET);
            return;
        }
        
        BookingComponent bookingComponent = new ConcreteBooking(flight, currentPassenger.name, seatCode);
        Booking booking = new Booking(bookingComponent);
        
        // Process payment
        if (paymentProcessor.processPayment(booking.getCost(), booking)) {
            currentPassenger.addBooking(booking);
            currentPassenger.subscribeToFlight(flight);
            System.out.println(ConsoleColors.GREEN + "Booking created: " + booking.getDescription() + " - Cost: $" + booking.getCost() + ConsoleColors.RESET);
            System.out.println(ConsoleColors.GREEN + "You are now subscribed to updates for Flight " + flight.getFlightNumber() + ConsoleColors.RESET);
        } else {
            // If payment fails, release the seat
            flight.getSeatMap().releaseSeat(seatCode);
            System.out.println(ConsoleColors.RED + "Booking cancelled due to payment failure." + ConsoleColors.RESET);
        }
    }

    private static List<Flight> filterByDateRange(List<Flight> flights) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        System.out.println(ConsoleColors.CYAN + "Enter date range (YYYY-MM-DD format)" + ConsoleColors.RESET);
        
        while (startDate == null) {
            System.out.print("Start date: ");
            String input = scanner.nextLine();
            try {
                startDate = LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println(ConsoleColors.RED + "Invalid date format. Please use YYYY-MM-DD." + ConsoleColors.RESET);
            }
        }
        
        while (endDate == null) {
            System.out.print("End date: ");
            String input = scanner.nextLine();
            try {
                endDate = LocalDate.parse(input, formatter);
                if (endDate.isBefore(startDate)) {
                    System.out.println(ConsoleColors.RED + "End date must be after start date." + ConsoleColors.RESET);
                    endDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println(ConsoleColors.RED + "Invalid date format. Please use YYYY-MM-DD." + ConsoleColors.RESET);
            }
        }
        
        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;
        
        List<Flight> filtered = new ArrayList<>();
        for (Flight flight : flights) {
            LocalDate flightDate = flight.getDepartureDate();
            if ((flightDate.isEqual(finalStartDate) || flightDate.isAfter(finalStartDate)) && 
                (flightDate.isEqual(finalEndDate) || flightDate.isBefore(finalEndDate))) {
                filtered.add(flight);
            }
        }
        
        System.out.println(ConsoleColors.GREEN + "Found " + filtered.size() + " flights between " + 
                           startDate + " and " + endDate + ConsoleColors.RESET);
        return filtered;
    }
    
    private static List<Flight> filterByMaxPrice(List<Flight> flights) {
        System.out.print("Enter maximum price (USD): ");
        double maxPrice;
        try {
            maxPrice = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.RED + "Invalid input. Not applying price filter." + ConsoleColors.RESET);
            return flights;
        }
        
        List<Flight> filtered = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getPrice() <= maxPrice) {
                filtered.add(flight);
            }
        }
        
        System.out.println(ConsoleColors.GREEN + "Found " + filtered.size() + " flights under $" + maxPrice + ConsoleColors.RESET);
        return filtered;
    }
    
    private static List<Flight> filterByAirline(List<Flight> flights) {
        // Get unique airlines from flights
        List<String> airlines = new ArrayList<>();
        for (Flight flight : flights) {
            if (!airlines.contains(flight.getAirline())) {
                airlines.add(flight.getAirline());
            }
        }
        
        System.out.println(ConsoleColors.CYAN + "Available airlines:" + ConsoleColors.RESET);
        for (int i = 0; i < airlines.size(); i++) {
            System.out.println((i + 1) + ". " + airlines.get(i));
        }
        
        System.out.print("Select airline (1-" + airlines.size() + "): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.RED + "Invalid input. Not applying airline filter." + ConsoleColors.RESET);
            return flights;
        }
        
        if (choice < 0 || choice >= airlines.size()) {
            System.out.println(ConsoleColors.RED + "Invalid selection. Not applying airline filter." + ConsoleColors.RESET);
            return flights;
        }
        
        String selectedAirline = airlines.get(choice);
        
        List<Flight> filtered = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getAirline().equals(selectedAirline)) {
                filtered.add(flight);
            }
        }
        
        System.out.println(ConsoleColors.GREEN + "Found " + filtered.size() + " flights by " + selectedAirline + ConsoleColors.RESET);
        return filtered;
    }
    
    private static List<Flight> filterByMaxDuration(List<Flight> flights) {
        System.out.print("Enter maximum flight duration in hours: ");
        double maxDuration;
        try {
            maxDuration = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.RED + "Invalid input. Not applying duration filter." + ConsoleColors.RESET);
            return flights;
        }
        
        List<Flight> filtered = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getDuration() <= maxDuration) {
                filtered.add(flight);
            }
        }
        
        System.out.println(ConsoleColors.GREEN + "Found " + filtered.size() + " flights under " + maxDuration + " hours" + ConsoleColors.RESET);
        return filtered;
    }
    
    private static void displayFlightsTable(List<Flight> flights) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        TableFormatter table = new TableFormatter(
            "Flight #", "Airline", "From", "To", "Date", "Time", "Duration", "Price", "Status"
        );
        
        for (Flight flight : flights) {
            String durationStr = String.format("%.1f hrs", flight.getDuration());
            String priceStr = String.format("$%.2f", flight.getPrice());
            
            if (flight.getDiscountPercentage() > 0) {
                priceStr += ConsoleColors.GREEN + " (-" + flight.getDiscountPercentage() + "%)" + ConsoleColors.RESET;
            }
            
            table.addRow(
                flight.getFlightNumber(),
                flight.getAirline(),
                flight.getDeparture(),
                flight.getArrival(),
                flight.getDepartureDate().format(dateFormatter),
                flight.getDepartureTime().format(timeFormatter),
                durationStr,
                priceStr,
                getColoredStatus(flight.getStatus())
            );
        }
        
        System.out.println(ConsoleColors.CYAN + "\n--- Found " + flights.size() + " flights ---" + ConsoleColors.RESET);
        System.out.println(table.render());
    }
    
    private static String getColoredStatus(String status) {
        switch (status) {
            case "On Time":
                return ConsoleColors.GREEN + status + ConsoleColors.RESET;
            case "Delayed":
                return ConsoleColors.YELLOW + status + ConsoleColors.RESET;
            case "Cancelled":
                return ConsoleColors.RED + status + ConsoleColors.RESET;
            case "Boarding":
                return ConsoleColors.CYAN + status + ConsoleColors.RESET;
            default:
                return status;
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
        
        // Use seat map visualizer to select a seat
        SeatMapVisualizer seatVisualizer = new SeatMapVisualizer(flight.getSeatMap());
        System.out.println(ConsoleColors.CYAN + "Please select your seat:" + ConsoleColors.RESET);
        String seatCode = seatVisualizer.selectSeat(scanner);
        
        if (seatCode == null) {
            System.out.println(ConsoleColors.YELLOW + "Booking cancelled." + ConsoleColors.RESET);
            return;
        }
        
        BookingComponent bookingComponent = new ConcreteBooking(flight, currentPassenger.name, seatCode);
        Booking booking = new Booking(bookingComponent);
        
        // Process payment
        if (paymentProcessor.processPayment(booking.getCost(), booking)) {
            currentPassenger.addBooking(booking);
            currentPassenger.subscribeToFlight(flight);
            System.out.println(ConsoleColors.GREEN + "Booking created: " + booking.getDescription() + " - Cost: $" + booking.getCost() + ConsoleColors.RESET);
            System.out.println(ConsoleColors.GREEN + "You are now subscribed to updates for Flight " + flightNumber + ConsoleColors.RESET);
        } else {
            // If payment fails, release the seat
            flight.getSeatMap().releaseSeat(seatCode);
            System.out.println(ConsoleColors.RED + "Booking cancelled due to payment failure." + ConsoleColors.RESET);
        }
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
        System.out.println("Enter 'back' to return to main menu");
        System.out.print("Enter booking number or flight number: ");
        
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("back")) {
            System.out.println(ConsoleColors.YELLOW + "Returning to main menu." + ConsoleColors.RESET);
            return;
        }
        
        Booking selectedBooking = null;
        
        // First try to parse as a numeric index
        try {
            int bookingIndex = Integer.parseInt(input) - 1;
            if (bookingIndex >= 0 && bookingIndex < currentPassenger.getBookings().size()) {
                selectedBooking = currentPassenger.getBookings().get(bookingIndex);
            } else {
                System.out.println(ConsoleColors.RED + "Invalid booking number. Please select a number between 1 and " + 
                                  currentPassenger.getBookings().size() + "." + ConsoleColors.RESET);
                return;
            }
        } catch (NumberFormatException e) {
            // If not a number, try to match against flight numbers or description
            for (Booking booking : currentPassenger.getBookings()) {
                if (booking.getFlight().getFlightNumber().equalsIgnoreCase(input) || 
                    booking.getDescription().toLowerCase().contains(input.toLowerCase())) {
                    selectedBooking = booking;
                    break;
                }
            }
            
            if (selectedBooking == null) {
                System.out.println(ConsoleColors.RED + "No booking found matching '" + input + "'. Please enter a valid booking number or flight number." + ConsoleColors.RESET);
                return;
            }
        }
        
        BookingComponent bookingComponent = selectedBooking.bookingComponent;
        double originalCost = selectedBooking.getCost();
        
        // Track selected services
        boolean hasInsurance = false;
        boolean hasMeal = false;
        boolean hasPriorityBoarding = false;
        
        System.out.println(ConsoleColors.CYAN + "\n=== Customize your booking with additional services ===" + ConsoleColors.RESET);
        
        while (true) {
            // Calculate current selections and price
            double currentTotalCost = originalCost;
            double addOnsTotal = 0;
            
            if (hasInsurance) addOnsTotal += 50.0;
            if (hasMeal) addOnsTotal += 20.0;
            if (hasPriorityBoarding) addOnsTotal += 30.0;
            
            currentTotalCost += addOnsTotal;
            
            // Show the booking status and selections
            System.out.println(ConsoleColors.YELLOW + "\n--- Current Selection Summary ---" + ConsoleColors.RESET);
            System.out.println("Base booking: " + selectedBooking.getDescription());
            System.out.println("Base price: $" + String.format("%.2f", originalCost));
            
            if (hasInsurance || hasMeal || hasPriorityBoarding) {
                System.out.println("\nSelected services:");
                if (hasInsurance) System.out.println("• Insurance (+$50.00)");
                if (hasMeal) System.out.println("• Meal (+$20.00)");
                if (hasPriorityBoarding) System.out.println("• Priority Boarding (+$30.00)");
                System.out.println("\nTotal add-ons: $" + String.format("%.2f", addOnsTotal));
            } else {
                System.out.println("\nNo services selected yet.");
            }
            
            System.out.println(ConsoleColors.GREEN + "Grand Total: $" + String.format("%.2f", currentTotalCost) + ConsoleColors.RESET);
            
            // Service selection menu
            System.out.println(ConsoleColors.CYAN + "\n--- Available Services ---" + ConsoleColors.RESET);
            
            // Protection Category
            System.out.println(ConsoleColors.YELLOW + "\n◆ Protection Services" + ConsoleColors.RESET);
            System.out.println("1. " + (hasInsurance ? "[✓] " : "[ ] ") + "Add Insurance (+$50.00)");
            System.out.println("   • Coverage for flight delays, cancellations, and lost baggage");
            System.out.println("   • 24/7 emergency assistance hotline");
            
            // Comfort Category
            System.out.println(ConsoleColors.YELLOW + "\n◆ Comfort Services" + ConsoleColors.RESET);
            System.out.println("2. " + (hasMeal ? "[✓] " : "[ ] ") + "Add Meal (+$20.00)");
            System.out.println("   • Choose from standard, vegetarian, vegan, or gluten-free options");
            System.out.println("   • Includes beverage and dessert");
            
            // Priority Category
            System.out.println(ConsoleColors.YELLOW + "\n◆ Priority Services" + ConsoleColors.RESET);
            System.out.println("3. " + (hasPriorityBoarding ? "[✓] " : "[ ] ") + "Add Priority Boarding (+$30.00)");
            System.out.println("   • Board the plane before general boarding");
            System.out.println("   • Guaranteed overhead bin space near your seat");
            
            System.out.println(ConsoleColors.CYAN + "\n--- Options ---" + ConsoleColors.RESET);
            System.out.println("4. Save and apply these selections");
            System.out.println("5. Cancel and return to main menu");
            
            System.out.print("\nChoose an option (1-5): ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException ex) {
                System.out.println(ConsoleColors.RED + "Invalid input. Please enter a number." + ConsoleColors.RESET);
                continue;
            }
            
            switch (choice) {
                case 1: // Toggle insurance
                    hasInsurance = !hasInsurance;
                    System.out.println(ConsoleColors.GREEN + "Insurance " + (hasInsurance ? "added" : "removed") + ConsoleColors.RESET);
                    break;
                    
                case 2: // Toggle meal
                    hasMeal = !hasMeal;
                    System.out.println(ConsoleColors.GREEN + "Meal " + (hasMeal ? "added" : "removed") + ConsoleColors.RESET);
                    break;
                    
                case 3: // Toggle priority boarding
                    hasPriorityBoarding = !hasPriorityBoarding;
                    System.out.println(ConsoleColors.GREEN + "Priority Boarding " + (hasPriorityBoarding ? "added" : "removed") + ConsoleColors.RESET);
                    break;
                    
                case 4: // Save selections
                    // Apply the selections to booking component
                    BookingComponent updatedComponent = selectedBooking.bookingComponent;
                    
                    if (hasInsurance) {
                        updatedComponent = new InsuranceDecorator(updatedComponent);
                    }
                    if (hasMeal) {
                        updatedComponent = new MealDecorator(updatedComponent);
                    }
                    if (hasPriorityBoarding) {
                        updatedComponent = new PriorityBoardingDecorator(updatedComponent);
                    }
                    
                    selectedBooking.setBookingComponent(updatedComponent);
                    double newCost = selectedBooking.getCost();
                    double additionalCost = newCost - originalCost;
                    
                    if (additionalCost > 0) {
                        System.out.println(ConsoleColors.YELLOW + "Additional cost for services: $" + 
                                          String.format("%.2f", additionalCost) + ConsoleColors.RESET);
                        
                        // Process payment for the additional services only if there are any
                        if (paymentProcessor.processPayment(additionalCost, selectedBooking)) {
                            // Add loyalty points for the additional purchase
                            currentPassenger.addLoyaltyPoints((int) (additionalCost / 10));
                            System.out.println(ConsoleColors.GREEN + "Final Booking: " + selectedBooking.getDescription() + 
                                              " - Total Cost: $" + String.format("%.2f", selectedBooking.getCost()) + ConsoleColors.RESET);
                        } else {
                            // If payment fails, revert to original booking without the new services
                            selectedBooking.setBookingComponent(bookingComponent);
                            System.out.println(ConsoleColors.RED + "Customization cancelled due to payment failure. Original booking preserved." + ConsoleColors.RESET);
                        }
                    } else {
                        System.out.println(ConsoleColors.GREEN + "No changes were made to your booking." + ConsoleColors.RESET);
                    }
                    return;
                    
                case 5: // Cancel
                    System.out.println(ConsoleColors.YELLOW + "Customization cancelled. Returning to main menu." + ConsoleColors.RESET);
                    return;
                    
                default:
                    System.out.println(ConsoleColors.RED + "Invalid choice. Please select a number between 1 and 5." + ConsoleColors.RESET);
            }
        }
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
        System.out.println("Enter 'back' to return to main menu");
        System.out.print("Enter booking number or flight number: ");
        
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("back")) {
            System.out.println(ConsoleColors.YELLOW + "Returning to main menu." + ConsoleColors.RESET);
            return;
        }
        
        Booking selectedBooking = null;
        
        // First try to parse as a numeric index
        try {
            int bookingIndex = Integer.parseInt(input) - 1;
            if (bookingIndex >= 0 && bookingIndex < currentPassenger.getBookings().size()) {
                selectedBooking = currentPassenger.getBookings().get(bookingIndex);
            } else {
                System.out.println(ConsoleColors.RED + "Invalid booking number. Please select a number between 1 and " + 
                                  currentPassenger.getBookings().size() + "." + ConsoleColors.RESET);
                return;
            }
        } catch (NumberFormatException e) {
            // If not a number, try to match against flight numbers
            for (Booking booking : currentPassenger.getBookings()) {
                if (booking.getFlight().getFlightNumber().equalsIgnoreCase(input) || 
                    booking.getDescription().toLowerCase().contains(input.toLowerCase())) {
                    selectedBooking = booking;
                    break;
                }
            }
            
            if (selectedBooking == null) {
                System.out.println(ConsoleColors.RED + "No booking found matching '" + input + "'. Please enter a valid booking number or flight number." + ConsoleColors.RESET);
                return;
            }
        }
        
        System.out.println(ConsoleColors.YELLOW + "Are you sure you want to cancel this booking? (y/n)" + ConsoleColors.RESET);
        System.out.println(selectedBooking.getDescription() + " - Cost: $" + selectedBooking.getCost());
        String confirm = scanner.nextLine().toLowerCase();
        
        if (confirm.equals("y")) {
            currentPassenger.cancelBooking(selectedBooking);
            System.out.println(ConsoleColors.GREEN + "Booking canceled successfully." + ConsoleColors.RESET);
            
            // Refund policy information
            System.out.println(ConsoleColors.CYAN + "Refund Policy:" + ConsoleColors.RESET);
            System.out.println("- 100% refund if canceled 7+ days before departure");
            System.out.println("- 50% refund if canceled 3-6 days before departure");
            System.out.println("- No refund if canceled less than 3 days before departure");
            System.out.println(ConsoleColors.YELLOW + "Please allow 5-7 business days for the refund to be processed." + ConsoleColors.RESET);
        } else {
            System.out.println(ConsoleColors.YELLOW + "Cancellation aborted." + ConsoleColors.RESET);
        }
    }

    private static void checkFlightStatus() {
        System.out.print("Enter flight number to check status: ");
        String flightNumber = scanner.nextLine();
        Flight flight = flightDb.getFlightByNumber(flightNumber);
        if (flight == null) {
            System.out.println(ConsoleColors.RED + "Flight not found." + ConsoleColors.RESET);
        } else {
            System.out.println(flight);
            
            // Check if user is subscribed to this flight
            boolean isSubscribed = false;
            for (Booking booking : currentPassenger.getBookings()) {
                if (booking.getFlight().getFlightNumber().equals(flightNumber)) {
                    isSubscribed = true;
                    break;
                }
            }
            
            if (!isSubscribed) {
                System.out.println(ConsoleColors.YELLOW + "Would you like to subscribe to updates for this flight? (y/n)" + ConsoleColors.RESET);
                String choice = scanner.nextLine().toLowerCase();
                if (choice.equals("y")) {
                    currentPassenger.subscribeToFlight(flight);
                    System.out.println(ConsoleColors.GREEN + "You are now subscribed to updates for Flight " + flightNumber + ConsoleColors.RESET);
                }
            }
        }
    }

    private static void viewLoyaltyPoints() {
        System.out.println(ConsoleColors.CYAN + "Your Loyalty Points: " + currentPassenger.getLoyaltyPoints() + ConsoleColors.RESET);
        
        // Show loyalty tiers and benefits
        System.out.println(ConsoleColors.CYAN + "\nLoyalty Program Tiers:" + ConsoleColors.RESET);
        System.out.println("- Bronze (0-100 points): No additional benefits");
        System.out.println("- Silver (101-500 points): Free seat selection, 5% discount on future bookings");
        System.out.println("- Gold (501-1000 points): Free seat selection, 10% discount, priority check-in");
        System.out.println("- Platinum (1001+ points): Free seat selection, 15% discount, priority check-in, lounge access");
        
        // Determine current tier
        int points = currentPassenger.getLoyaltyPoints();
        String tier;
        String benefits;
        
        if (points > 1000) {
            tier = "Platinum";
            benefits = "Free seat selection, 15% discount, priority check-in, lounge access";
        } else if (points > 500) {
            tier = "Gold";
            benefits = "Free seat selection, 10% discount, priority check-in";
        } else if (points > 100) {
            tier = "Silver";
            benefits = "Free seat selection, 5% discount on future bookings";
        } else {
            tier = "Bronze";
            benefits = "No additional benefits";
        }
        
        System.out.println(ConsoleColors.GREEN + "\nYour current tier: " + tier + ConsoleColors.RESET);
        System.out.println("Your benefits: " + benefits);
        
        if (!tier.equals("Platinum")) {
            int pointsToNextTier;
            String nextTier;
            
            if (tier.equals("Bronze")) {
                pointsToNextTier = 101 - points;
                nextTier = "Silver";
            } else if (tier.equals("Silver")) {
                pointsToNextTier = 501 - points;
                nextTier = "Gold";
            } else { // Gold
                pointsToNextTier = 1001 - points;
                nextTier = "Platinum";
            }
            
            System.out.println(ConsoleColors.YELLOW + "You need " + pointsToNextTier + 
                               " more points to reach " + nextTier + " tier." + ConsoleColors.RESET);
        }
    }

    private static void selectChangeSeats() {
        if (currentPassenger.getBookings().isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "You have no bookings to change seats for." + ConsoleColors.RESET);
            return;
        }
        
        System.out.println(ConsoleColors.CYAN + "Select a booking to change seats:" + ConsoleColors.RESET);
        for (int i = 0; i < currentPassenger.getBookings().size(); i++) {
            Booking booking = currentPassenger.getBookings().get(i);
            ConcreteBooking concreteBooking = null;
            
            // Get the concrete booking to access seat information
            if (booking.bookingComponent instanceof ConcreteBooking) {
                concreteBooking = (ConcreteBooking) booking.bookingComponent;
            }
            
            String seatInfo = (concreteBooking != null) ? 
                            " (Current seat: " + concreteBooking.getSeatCode() + ")" :
                            " (Seat information not available)";
                            
            Flight flight = booking.getFlight();
            String flightInfo = (flight != null) ? 
                              " [Flight " + flight.getFlightNumber() + "]" :
                              "";
                              
            System.out.println((i + 1) + ". " + booking.getDescription() + seatInfo + flightInfo);
        }
        
        System.out.println(ConsoleColors.YELLOW + "Enter booking number, flight number, or '0' to cancel:" + ConsoleColors.RESET);
        System.out.print("Your choice: ");
        String input = scanner.nextLine().trim();
        
        if (input.equals("0")) {
            System.out.println(ConsoleColors.YELLOW + "Returning to main menu." + ConsoleColors.RESET);
            return;
        }
        
        Booking selectedBooking = null;
        
        // First try to parse as a numeric index
        try {
            int bookingChoice = Integer.parseInt(input);
            if (bookingChoice >= 1 && bookingChoice <= currentPassenger.getBookings().size()) {
                selectedBooking = currentPassenger.getBookings().get(bookingChoice - 1);
            } else {
                System.out.println(ConsoleColors.RED + "Invalid booking number." + ConsoleColors.RESET);
                return;
            }
        } catch (NumberFormatException e) {
            // If not a number, search by flight number
            for (Booking booking : currentPassenger.getBookings()) {
                if (booking.getFlight() != null && 
                    booking.getFlight().getFlightNumber().equalsIgnoreCase(input)) {
                    selectedBooking = booking;
                    break;
                }
            }
            
            if (selectedBooking == null) {
                System.out.println(ConsoleColors.RED + "No booking found for flight number: " + input + ConsoleColors.RESET);
                return;
            }
        }
        
        if (!(selectedBooking.bookingComponent instanceof ConcreteBooking)) {
            System.out.println(ConsoleColors.RED + "Cannot change seat for this booking type." + ConsoleColors.RESET);
            return;
        }
        
        ConcreteBooking concreteBooking = (ConcreteBooking) selectedBooking.bookingComponent;
        Flight flight = concreteBooking.getFlight();
        
        // Display current seat and flight information
        System.out.println(ConsoleColors.CYAN + "Current booking information:" + ConsoleColors.RESET);
        System.out.println("Flight: " + flight.getFlightNumber() + " from " + flight.getDeparture() + " to " + flight.getArrival());
        System.out.println("Current seat: " + concreteBooking.getSeatCode());
        
        // Use seat map visualizer to select a new seat
        SeatMapVisualizer seatVisualizer = new SeatMapVisualizer(flight.getSeatMap());
        System.out.println(ConsoleColors.CYAN + "Please select your new seat:" + ConsoleColors.RESET);
        String newSeatCode = seatVisualizer.selectSeat(scanner);
        
        if (newSeatCode == null) {
            System.out.println(ConsoleColors.YELLOW + "Seat change cancelled." + ConsoleColors.RESET);
            return;
        }
        
        // Try to change the seat
        if (concreteBooking.changeSeat(newSeatCode)) {
            // Update the booking component with new seat information
            selectedBooking.setBookingComponent(concreteBooking);
            System.out.println(ConsoleColors.GREEN + "Seat changed successfully to " + newSeatCode + "." + ConsoleColors.RESET);
        } else {
            System.out.println(ConsoleColors.RED + "Failed to change seat. The seat may be occupied or invalid." + ConsoleColors.RESET);
        }
    }
}
