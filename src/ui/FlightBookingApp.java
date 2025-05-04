package ui;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

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
            int choice = getNumericChoice(1, currentUser.isAdmin() ? 11 : 10);
            switch (choice) {
                case 1:
                    searchFlights();
                    break;
                case 2:
                    bookFlight();
                    break;
                case 3:
                    createGroupBooking();
                    break;
                case 4:
                    customizeBooking();
                    break;
                case 5:
                    viewBookings();
                    break;
                case 6:
                    cancelBooking();
                    break;
                case 7:
                    checkFlightStatus();
                    break;
                case 8:
                    viewLoyaltyPoints();
                    break;
                case 9:
                    selectChangeSeats();
                    break;
                case 10:
                    if (currentUser.isAdmin()) {
                        showAdminPanel();
                    } else {
                        System.out.println(ConsoleColors.GREEN + "Thank you for using the system, " + 
                                         currentUser.getUsername() + "!" + ConsoleColors.RESET);
                        return;
                    }
                    break;
                case 11: // Only available for admin users
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
        System.out.println("3. Create Group Booking");
        System.out.println("4. Customize Booking");
        System.out.println("5. View My Bookings");
        System.out.println("6. Cancel a Booking");
        System.out.println("7. Check Flight Status");
        System.out.println("8. View Loyalty Points");
        System.out.println("9. Select/Change Seats");
        
        if (currentUser.isAdmin()) {
            System.out.println("10. Admin Panel");
            System.out.println("11. Exit");
        } else {
            System.out.println("10. Exit");
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
        System.out.println(ConsoleColors.CYAN + "\n--- My Bookings ---" + ConsoleColors.RESET);
        
        // Display individual bookings
        System.out.println(ConsoleColors.YELLOW + "\nIndividual Bookings:" + ConsoleColors.RESET);
        List<Booking> userBookings = currentPassenger.getBookings();
        
        if (userBookings.isEmpty()) {
            System.out.println("You have no individual bookings.");
        } else {
            for (int i = 0; i < userBookings.size(); i++) {
                Booking booking = userBookings.get(i);
                Flight flight = booking.getFlight();
                
                System.out.println((i+1) + ". " + booking.getDescription());
                System.out.println("   Price: $" + String.format("%.2f", booking.getCost()));
                System.out.println("   Flight Status: " + getColoredStatus(flight.getStatus()));
                System.out.println();
            }
        }
        
        // Display group bookings
        System.out.println(ConsoleColors.YELLOW + "\nGroup Bookings:" + ConsoleColors.RESET);
        data.GroupBookingDatabase groupDb = new data.GroupBookingDatabase();
        List<core.GroupBooking> groupBookings = groupDb.getGroupBookingsForPassenger(currentPassenger);
        
        if (groupBookings.isEmpty()) {
            System.out.println("You have no group bookings.");
        } else {
            for (int i = 0; i < groupBookings.size(); i++) {
                core.GroupBooking groupBooking = groupBookings.get(i);
                Flight flight = groupBooking.getFlight();
                
                System.out.println((i+1) + ". Group ID: " + groupBooking.getGroupId());
                System.out.println("   Flight: " + flight.getFlightNumber() + 
                                  " (" + flight.getDeparture() + " to " + flight.getArrival() + ")");
                System.out.println("   Date: " + flight.getDepartureDate());
                System.out.println("   Passengers: " + groupBooking.getPassengerCount());
                System.out.println("   Total Cost: $" + String.format("%.2f", groupBooking.getTotalCost()));
                System.out.println("   Flight Status: " + getColoredStatus(flight.getStatus()));
                System.out.println("   Contact: " + groupBooking.getContactName());
                
                // Display seat assignments
                System.out.println(ConsoleColors.CYAN + "   Seat Assignments:" + ConsoleColors.RESET);
                Map<Passenger, String> seatAssignments = groupBooking.getAllSeatAssignments();
                for (Passenger passenger : groupBooking.getPassengers()) {
                    String seat = seatAssignments.get(passenger);
                    String seatInfo = seat != null ? seat : "Not assigned";
                    System.out.println("   • " + passenger.name + ": " + seatInfo);
                }
                
                System.out.println();
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void cancelBooking() {
        System.out.println(ConsoleColors.CYAN + "\n--- Cancel Booking ---" + ConsoleColors.RESET);
        
        // First, display all individual bookings
        System.out.println(ConsoleColors.YELLOW + "Individual Bookings:" + ConsoleColors.RESET);
        List<Booking> userBookings = currentPassenger.getBookings();
        
        if (userBookings.isEmpty()) {
            System.out.println("You have no individual bookings to cancel.");
        } else {
            for (int i = 0; i < userBookings.size(); i++) {
                Booking booking = userBookings.get(i);
                System.out.println((i+1) + ". " + booking.getDescription());
            }
        }
        
        // Next, display all group bookings
        System.out.println(ConsoleColors.YELLOW + "\nGroup Bookings:" + ConsoleColors.RESET);
        data.GroupBookingDatabase groupDb = new data.GroupBookingDatabase();
        List<core.GroupBooking> groupBookings = groupDb.getGroupBookingsForPassenger(currentPassenger);
        
        if (groupBookings.isEmpty()) {
            System.out.println("You have no group bookings to cancel.");
        } else {
            for (int i = 0; i < groupBookings.size(); i++) {
                core.GroupBooking groupBooking = groupBookings.get(i);
                Flight flight = groupBooking.getFlight();
                
                System.out.println((userBookings.size() + i + 1) + ". Group ID: " + groupBooking.getGroupId() + 
                                  " - Flight: " + flight.getFlightNumber() + 
                                  " (" + flight.getDeparture() + " to " + flight.getArrival() + ")");
            }
        }
        
        if (userBookings.isEmpty() && groupBookings.isEmpty()) {
            System.out.println(ConsoleColors.RED + "You don't have any bookings to cancel." + ConsoleColors.RESET);
            return;
        }
        
        System.out.println("\n1. Cancel Individual Booking");
        System.out.println("2. Cancel Group Booking");
        System.out.println("3. Go Back");
        System.out.print("Choose an option: ");
        int choice;
        
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.RED + "Invalid input. Returning to main menu." + ConsoleColors.RESET);
            return;
        }
        
        if (choice == 1) {
            if (userBookings.isEmpty()) {
                System.out.println(ConsoleColors.RED + "You have no individual bookings to cancel." + ConsoleColors.RESET);
                return;
            }
            
            System.out.print("Enter the number of the booking to cancel: ");
            int bookingIndex;
            
            try {
                bookingIndex = Integer.parseInt(scanner.nextLine()) - 1;
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.RED + "Invalid input. Returning to main menu." + ConsoleColors.RESET);
                return;
            }
            
            if (bookingIndex < 0 || bookingIndex >= userBookings.size()) {
                System.out.println(ConsoleColors.RED + "Invalid booking number." + ConsoleColors.RESET);
                return;
            }
            
            Booking bookingToCancel = userBookings.get(bookingIndex);
            System.out.println("You're about to cancel: " + bookingToCancel.getDescription());
            System.out.println(ConsoleColors.RED + "This action cannot be undone." + ConsoleColors.RESET);
            System.out.print("Type 'confirm' to proceed with cancellation: ");
            String confirmation = scanner.nextLine();
            
            if (confirmation.equalsIgnoreCase("confirm")) {
                // If the booking has a seat, release it
                BookingComponent component = bookingToCancel.bookingComponent;
                if (component instanceof ConcreteBooking) {
                    ConcreteBooking concreteBooking = (ConcreteBooking) component;
                    if (concreteBooking.getSeatCode() != null && !concreteBooking.getSeatCode().isEmpty()) {
                        bookingToCancel.getFlight().getSeatMap().releaseSeat(concreteBooking.getSeatCode());
                    }
                }
                
                // Remove the booking from the passenger's list
                currentPassenger.cancelBooking(bookingToCancel);
                
                System.out.println(ConsoleColors.GREEN + "Booking canceled successfully." + ConsoleColors.RESET);
            } else {
                System.out.println(ConsoleColors.YELLOW + "Cancellation aborted." + ConsoleColors.RESET);
            }
        } else if (choice == 2) {
            if (groupBookings.isEmpty()) {
                System.out.println(ConsoleColors.RED + "You have no group bookings to cancel." + ConsoleColors.RESET);
                return;
            }
            
            System.out.print("Enter the number of the group booking to cancel: ");
            int groupIndex;
            
            try {
                groupIndex = Integer.parseInt(scanner.nextLine()) - 1;
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.RED + "Invalid input. Returning to main menu." + ConsoleColors.RESET);
                return;
            }
            
            if (groupIndex < 0 || groupIndex >= groupBookings.size()) {
                System.out.println(ConsoleColors.RED + "Invalid group booking number." + ConsoleColors.RESET);
                return;
            }
            
            core.GroupBooking groupBookingToCancel = groupBookings.get(groupIndex);
            System.out.println("You're about to cancel: " + groupBookingToCancel.getGroupId());
            System.out.println(ConsoleColors.RED + "This action cannot be undone." + ConsoleColors.RESET);
            System.out.print("Type 'confirm' to proceed with cancellation: ");
            String groupConfirmation = scanner.nextLine();
            
            if (groupConfirmation.equalsIgnoreCase("confirm")) {
                // Remove the group booking from the passenger's list
                currentPassenger.cancelGroupBooking(groupBookingToCancel);
                
                System.out.println(ConsoleColors.GREEN + "Group booking canceled successfully." + ConsoleColors.RESET);
            } else {
                System.out.println(ConsoleColors.YELLOW + "Cancellation aborted." + ConsoleColors.RESET);
            }
        } else if (choice == 3) {
            System.out.println(ConsoleColors.YELLOW + "Returning to main menu." + ConsoleColors.RESET);
            return;
        } else {
            System.out.println(ConsoleColors.RED + "Invalid choice. Please select a number between 1 and 3." + ConsoleColors.RESET);
        }
    }

    private static void checkFlightStatus() {
        System.out.println(ConsoleColors.CYAN + "\n--- Check Flight Status ---" + ConsoleColors.RESET);
        
        System.out.print("Enter flight number: ");
        String flightNumber = scanner.nextLine();
        Flight flight = flightDb.getFlightByNumber(flightNumber);
        
        if (flight == null) {
            System.out.println(ConsoleColors.RED + "Flight not found." + ConsoleColors.RESET);
            return;
        }
        
        System.out.println("Flight Status: " + getColoredStatus(flight.getStatus()));
    }

    private static void viewLoyaltyPoints() {
        System.out.println(ConsoleColors.CYAN + "\n--- View Loyalty Points ---" + ConsoleColors.RESET);
        
        System.out.println("Current Loyalty Points: " + currentPassenger.getLoyaltyPoints());
    }

    private static void selectChangeSeats() {
        System.out.println(ConsoleColors.CYAN + "\n--- Select/Change Seats ---" + ConsoleColors.RESET);
        
        System.out.println("1. Change seat for individual booking");
        System.out.println("2. Change seat for group booking");
        System.out.println("3. Go back");
        System.out.print("Choose an option: ");
        
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.RED + "Invalid input. Returning to main menu." + ConsoleColors.RESET);
            return;
        }
        
        switch (choice) {
            case 1:
                changeIndividualSeat();
                break;
            case 2:
                changeGroupSeat();
                break;
            case 3:
                return;
            default:
                System.out.println(ConsoleColors.RED + "Invalid choice. Please try again." + ConsoleColors.RESET);
        }
    }
    
    /**
     * Change seat for an individual booking
     */
    private static void changeIndividualSeat() {
        System.out.print("Enter flight number: ");
        String flightNumber = scanner.nextLine();
        Flight flight = flightDb.getFlightByNumber(flightNumber);
        
        if (flight == null) {
            System.out.println(ConsoleColors.RED + "Flight not found." + ConsoleColors.RESET);
            return;
        }
        
        // Check if user has a booking for this flight
        Booking userBooking = null;
        for (Booking booking : currentPassenger.getBookings()) {
            if (booking.getFlight().getFlightNumber().equals(flightNumber)) {
                userBooking = booking;
                break;
            }
        }
        
        if (userBooking == null) {
            System.out.println(ConsoleColors.RED + "You don't have a booking for flight " + flightNumber + ConsoleColors.RESET);
            return;
        }
        
        // Get current seat assignment
        String currentSeat = null;
        if (userBooking.bookingComponent instanceof ConcreteBooking) {
            currentSeat = ((ConcreteBooking) userBooking.bookingComponent).getSeatCode();
            System.out.println(ConsoleColors.YELLOW + "Current seat assignment: " + currentSeat + ConsoleColors.RESET);
        }
        
        // Use seat map visualizer to select a new seat
        SeatMapVisualizer seatVisualizer = new SeatMapVisualizer(flight.getSeatMap());
        System.out.println(ConsoleColors.CYAN + "Please select your new seat:" + ConsoleColors.RESET);
        String newSeatCode = seatVisualizer.selectSeat(scanner);
        
        if (newSeatCode == null) {
            System.out.println(ConsoleColors.YELLOW + "Seat change cancelled." + ConsoleColors.RESET);
            return;
        }
        
        // Change the seat
        boolean seatChanged = false;
        if (userBooking.bookingComponent instanceof ConcreteBooking) {
            seatChanged = ((ConcreteBooking) userBooking.bookingComponent).changeSeat(newSeatCode);
        }
        
        if (seatChanged) {
            System.out.println(ConsoleColors.GREEN + "Seat changed successfully from " + currentSeat + " to " + newSeatCode + ConsoleColors.RESET);
        } else {
            System.out.println(ConsoleColors.RED + "Failed to change seat. The seat may already be occupied." + ConsoleColors.RESET);
        }
    }
    
    /**
     * Change seat for a passenger in a group booking
     */
    private static void changeGroupSeat() {
        data.GroupBookingDatabase groupDb = new data.GroupBookingDatabase();
        List<core.GroupBooking> groupBookings = groupDb.getGroupBookingsForPassenger(currentPassenger);
        
        if (groupBookings.isEmpty()) {
            System.out.println(ConsoleColors.RED + "You don't have any group bookings." + ConsoleColors.RESET);
            return;
        }
        
        // Display group bookings
        System.out.println(ConsoleColors.CYAN + "Your group bookings:" + ConsoleColors.RESET);
        for (int i = 0; i < groupBookings.size(); i++) {
            core.GroupBooking groupBooking = groupBookings.get(i);
            Flight flight = groupBooking.getFlight();
            
            System.out.println((i+1) + ". Group ID: " + groupBooking.getGroupId() + 
                              " - Flight: " + flight.getFlightNumber() + 
                              " (" + flight.getDeparture() + " to " + flight.getArrival() + ")");
        }
        
        System.out.print("Select a group booking (1-" + groupBookings.size() + "): ");
        int groupIndex;
        try {
            groupIndex = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.RED + "Invalid input. Returning to main menu." + ConsoleColors.RESET);
            return;
        }
        
        if (groupIndex < 0 || groupIndex >= groupBookings.size()) {
            System.out.println(ConsoleColors.RED + "Invalid selection." + ConsoleColors.RESET);
            return;
        }
        
        core.GroupBooking selectedGroupBooking = groupBookings.get(groupIndex);
        Flight flight = selectedGroupBooking.getFlight();
        
        // Select the passenger to change seat for
        List<Passenger> passengers = selectedGroupBooking.getPassengers();
        System.out.println(ConsoleColors.CYAN + "Passengers in this booking:" + ConsoleColors.RESET);
        
        // Display current seat assignments
        Map<Passenger, String> seatAssignments = selectedGroupBooking.getAllSeatAssignments();
        for (int i = 0; i < passengers.size(); i++) {
            Passenger passenger = passengers.get(i);
            String seatCode = seatAssignments.get(passenger);
            String seatInfo = seatCode != null ? seatCode : "Not assigned";
            System.out.println((i+1) + ". " + passenger.name + " (Current seat: " + seatInfo + ")");
        }
        
        System.out.print("Select a passenger (1-" + passengers.size() + "): ");
        int passengerIndex;
        try {
            passengerIndex = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.RED + "Invalid input. Returning to main menu." + ConsoleColors.RESET);
            return;
        }
        
        if (passengerIndex < 0 || passengerIndex >= passengers.size()) {
            System.out.println(ConsoleColors.RED + "Invalid selection." + ConsoleColors.RESET);
            return;
        }
        
        Passenger selectedPassenger = passengers.get(passengerIndex);
        String currentSeat = seatAssignments.get(selectedPassenger);
        
        // Use seat map visualizer to select a new seat
        SeatMapVisualizer seatVisualizer = new SeatMapVisualizer(flight.getSeatMap());
        System.out.println(ConsoleColors.CYAN + "Please select a new seat for " + selectedPassenger.name + ":" + ConsoleColors.RESET);
        String newSeatCode = seatVisualizer.selectSeat(scanner);
        
        if (newSeatCode == null) {
            System.out.println(ConsoleColors.YELLOW + "Seat change cancelled." + ConsoleColors.RESET);
            return;
        }
        
        // Change the seat
        boolean seatChanged = selectedGroupBooking.assignSeat(selectedPassenger, newSeatCode);
        
        if (seatChanged) {
            // Save the updated group booking
            groupDb.updateGroupBooking(selectedGroupBooking);
            
            System.out.println(ConsoleColors.GREEN + "Seat changed successfully for " + selectedPassenger.name + 
                               " from " + (currentSeat != null ? currentSeat : "not assigned") + 
                               " to " + newSeatCode + ConsoleColors.RESET);
        } else {
            System.out.println(ConsoleColors.RED + "Failed to change seat. The seat may already be occupied." + ConsoleColors.RESET);
        }
    }

    /**
     * Creates a new group booking for multiple passengers.
     */
    private static void createGroupBooking() {
        System.out.println(ConsoleColors.CYAN + "\n--- Create Group Booking ---" + ConsoleColors.RESET);
        
        // Initialize the database
        data.GroupBookingDatabase groupDb = new data.GroupBookingDatabase();
        
        // Step 1: Search for a flight
        System.out.println(ConsoleColors.YELLOW + "Step 1: Select a flight for the group" + ConsoleColors.RESET);
        List<Flight> searchResults = searchFlightsForBooking();
        
        if (searchResults == null || searchResults.isEmpty()) {
            System.out.println(ConsoleColors.RED + "No flights available for booking." + ConsoleColors.RESET);
            return;
        }
        
        // Display flights and let user select one
        displayFlightsTable(searchResults);
        
        System.out.print("Enter the flight number you want to book (or 'back' to return): ");
        String flightNumber = scanner.nextLine().toUpperCase();
        
        if (flightNumber.equalsIgnoreCase("back")) {
            return;
        }
        
        // Find the selected flight
        Flight selectedFlight = null;
        for (Flight flight : searchResults) {
            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                selectedFlight = flight;
                break;
            }
        }
        
        if (selectedFlight == null) {
            System.out.println(ConsoleColors.RED + "Invalid flight number. Please try again." + ConsoleColors.RESET);
            return;
        }
        
        // Step 2: Enter group details
        System.out.println(ConsoleColors.YELLOW + "\nStep 2: Enter group details" + ConsoleColors.RESET);
        
        System.out.println("Selected flight: " + selectedFlight.getFlightNumber() + 
                          " from " + selectedFlight.getDeparture() + 
                          " to " + selectedFlight.getArrival() + 
                          " on " + selectedFlight.getDepartureDate());
        
        // Contact information
        System.out.print("Enter contact name: ");
        String contactName = scanner.nextLine();
        
        System.out.print("Enter contact email: ");
        String contactEmail = scanner.nextLine();
        
        System.out.print("Enter contact phone: ");
        String contactPhone = scanner.nextLine();
        
        // Step 3: Add passengers to the group
        System.out.println(ConsoleColors.YELLOW + "\nStep 3: Add passengers to the group" + ConsoleColors.RESET);
        
        List<Passenger> passengers = new ArrayList<>();
        // Add the current user's passenger by default
        passengers.add(currentPassenger);
        System.out.println("Added: " + currentPassenger.name + " (You)");
        
        while (true) {
            System.out.println("\nCurrent number of passengers: " + passengers.size());
            System.out.println("1. Add another passenger");
            System.out.println("2. Continue to seat selection");
            System.out.print("Choose an option: ");
            
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.RED + "Invalid input. Please enter a number." + ConsoleColors.RESET);
                continue;
            }
            
            if (choice == 1) {
                System.out.print("Enter passenger name: ");
                String passengerName = scanner.nextLine();
                
                if (passengerName.isEmpty()) {
                    System.out.println(ConsoleColors.RED + "Passenger name cannot be empty." + ConsoleColors.RESET);
                    continue;
                }
                
                Passenger newPassenger = new Passenger(passengerName);
                passengers.add(newPassenger);
                System.out.println(ConsoleColors.GREEN + "Added: " + passengerName + ConsoleColors.RESET);
            } else if (choice == 2) {
                if (passengers.size() < 2) {
                    System.out.println(ConsoleColors.RED + "A group booking must have at least 2 passengers." + ConsoleColors.RESET);
                    continue;
                }
                break;
            } else {
                System.out.println(ConsoleColors.RED + "Invalid choice. Please try again." + ConsoleColors.RESET);
            }
        }
        
        // Step 4: Create the group booking without seat assignments
        String groupId = groupDb.generateGroupId();
        core.GroupBooking groupBooking = new core.GroupBooking(
            groupId, selectedFlight, passengers, 0.0, // Start with 0% discount, will update later
            contactName, contactEmail, contactPhone
        );
        
        // Step 5: Seat selection for each passenger
        System.out.println(ConsoleColors.YELLOW + "\nStep 5: Select seats for each passenger" + ConsoleColors.RESET);
        
        SeatMapVisualizer seatVisualizer = new SeatMapVisualizer(selectedFlight.getSeatMap());
        
        for (Passenger passenger : passengers) {
            System.out.println(ConsoleColors.CYAN + "\nSelecting seat for: " + passenger.name + ConsoleColors.RESET);
            
            // Display seat map and let the user select a seat
            String seatCode = seatVisualizer.selectSeat(scanner);
            
            if (seatCode == null) {
                System.out.println(ConsoleColors.RED + "Seat selection cancelled. Group booking aborted." + ConsoleColors.RESET);
                return;
            }
            
            // Assign the selected seat to the passenger
            if (!groupBooking.assignSeat(passenger, seatCode)) {
                System.out.println(ConsoleColors.RED + "Error assigning seat " + seatCode + " to " + passenger.name + ". Please try again." + ConsoleColors.RESET);
                return;
            }
            
            System.out.println(ConsoleColors.GREEN + "Seat " + seatCode + " assigned to " + passenger.name + ConsoleColors.RESET);
        }
        
        // Step 6: Calculate price and apply group discount
        System.out.println(ConsoleColors.YELLOW + "\nStep 6: Group Discount" + ConsoleColors.RESET);
        
        // Calculate discount based on group size
        double discountPercentage = calculateGroupDiscount(passengers.size());
        
        // Update the discount in the group booking
        groupBooking.setGroupDiscountPercentage(discountPercentage);
        
        double originalPrice = selectedFlight.getPrice() * passengers.size();
        double discountedPrice = groupBooking.getTotalCost();
        
        System.out.println("Group size: " + passengers.size() + " passengers");
        System.out.println("Original total price: $" + String.format("%.2f", originalPrice));
        System.out.println("Group discount: " + discountPercentage + "%");
        System.out.println("Final price: $" + String.format("%.2f", discountedPrice));
        
        // Step 7: Confirm and process payment
        System.out.println(ConsoleColors.YELLOW + "\nStep 7: Confirm Booking" + ConsoleColors.RESET);
        System.out.println("1. Proceed to payment");
        System.out.println("2. Cancel");
        System.out.print("Choose an option: ");
        
        int confirmChoice;
        try {
            confirmChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.RED + "Invalid input. Booking cancelled." + ConsoleColors.RESET);
            return;
        }
        
        if (confirmChoice != 1) {
            System.out.println(ConsoleColors.YELLOW + "Booking cancelled." + ConsoleColors.RESET);
            return;
        }
        
        // Process payment
        if (!paymentProcessor.processPayment(discountedPrice, "Group booking for " + passengers.size() + " passengers to " + selectedFlight.getArrival())) {
            System.out.println(ConsoleColors.RED + "Payment failed. Group booking cancelled." + ConsoleColors.RESET);
            return;
        }
        
        // Step 8: Save the group booking
        if (groupDb.addGroupBooking(groupBooking)) {
            System.out.println(ConsoleColors.GREEN + "\nGroup booking created successfully!" + ConsoleColors.RESET);
            System.out.println("Group ID: " + groupId);
            System.out.println("Flight: " + selectedFlight.getFlightNumber() + 
                              " from " + selectedFlight.getDeparture() + 
                              " to " + selectedFlight.getArrival());
            System.out.println("Date: " + selectedFlight.getDepartureDate());
            System.out.println("Passengers: " + passengers.size());
            System.out.println("Contact: " + contactName);
            System.out.println("Total paid: $" + String.format("%.2f", discountedPrice));
        } else {
            System.out.println(ConsoleColors.RED + "Error creating group booking. Please try again." + ConsoleColors.RESET);
        }
    }

    /**
     * Calculate group discount percentage based on group size.
     * 
     * @param groupSize Number of passengers in the group
     * @return Discount percentage
     */
    private static double calculateGroupDiscount(int groupSize) {
        if (groupSize >= 10) {
            return 15.0; // 15% discount for 10+ passengers
        } else if (groupSize >= 5) {
            return 10.0; // 10% discount for 5-9 passengers
        } else if (groupSize >= 3) {
            return 5.0;  // 5% discount for 3-4 passengers
        } else {
            return 2.0;  // 2% discount for 2 passengers
        }
    }

    /**
     * Searches for flights for booking purposes.
     * 
     * @return List of flights matching the search criteria
     */
    private static List<Flight> searchFlightsForBooking() {
        List<String> departures = flightDb.getAvailableDepartures();
        if (departures.isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "No departure cities found in the database." + ConsoleColors.RESET);
            return null;
        }
        
        displayStringList(departures, "Available Departure Cities");
        System.out.print("Enter departure city code: ");
        String selectedDeparture = scanner.nextLine().toUpperCase();
        
        List<String> arrivals = flightDb.getAvailableArrivalsFrom(selectedDeparture);
        if (arrivals.isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "No arrival cities found for the selected departure." + ConsoleColors.RESET);
            return null;
        }
        
        displayStringList(arrivals, "Available Arrival Cities");
        System.out.print("Enter arrival city code: ");
        String selectedArrival = scanner.nextLine().toUpperCase();
        
        List<Flight> results = flightDb.searchFlights(selectedDeparture, selectedArrival);
        if (results.isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "No flights found between " + selectedDeparture + 
                              " and " + selectedArrival + "." + ConsoleColors.RESET);
            return null;
        }
        
        return results;
    }
}
