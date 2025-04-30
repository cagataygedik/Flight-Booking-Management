import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main {
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
            int choice = getNumericChoice(1, currentUser.isAdmin() ? 9 : 8);
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
                    if (currentUser.isAdmin()) {
                        showAdminPanel();
                    } else {
                        System.out.println(ConsoleColors.GREEN + "Thank you for using the system, " + 
                                           currentUser.getUsername() + "!" + ConsoleColors.RESET);
                        return;
                    }
                    break;
                case 9: // Only available for admin users
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
        
        if (currentUser.isAdmin()) {
            System.out.println("8. Admin Panel");
            System.out.println("9. Exit");
        } else {
            System.out.println("8. Exit");
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
            }
        }
    }
    
    /**
     * Helper method to book a specific flight selected from search results
     */
    private static void bookSpecificFlight(Flight flight) {
        System.out.println(ConsoleColors.CYAN + "Booking Flight: " + flight.getFlightNumber() + 
                           " from " + flight.getDeparture() + " to " + flight.getArrival() + ConsoleColors.RESET);
        
        System.out.print("Select seat (window, aisle, middle): ");
        String seat = scanner.nextLine();
        BookingComponent bookingComponent = new ConcreteBooking(flight, currentPassenger.name, seat);
        Booking booking = new Booking(bookingComponent);
        
        // Process payment
        if (paymentProcessor.processPayment(booking.getCost(), booking)) {
            currentPassenger.addBooking(booking);
            currentPassenger.subscribeToFlight(flight);
            System.out.println(ConsoleColors.GREEN + "Booking created: " + booking.getDescription() + " - Cost: $" + booking.getCost() + ConsoleColors.RESET);
            System.out.println(ConsoleColors.GREEN + "You are now subscribed to updates for Flight " + flight.getFlightNumber() + ConsoleColors.RESET);
        } else {
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
        System.out.print("Select seat (window, aisle, middle): ");
        String seat = scanner.nextLine();
        BookingComponent bookingComponent = new ConcreteBooking(flight, currentPassenger.name, seat);
        Booking booking = new Booking(bookingComponent);
        
        // Process payment
        if (paymentProcessor.processPayment(booking.getCost(), booking)) {
            currentPassenger.addBooking(booking);
            currentPassenger.subscribeToFlight(flight);
            System.out.println(ConsoleColors.GREEN + "Booking created: " + booking.getDescription() + " - Cost: $" + booking.getCost() + ConsoleColors.RESET);
            System.out.println(ConsoleColors.GREEN + "You are now subscribed to updates for Flight " + flightNumber + ConsoleColors.RESET);
        } else {
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
        System.out.print("Enter booking number: ");
        int bookingIndex = scanner.nextInt() - 1;
        scanner.nextLine();
        if (bookingIndex < 0 || bookingIndex >= currentPassenger.getBookings().size()) {
            System.out.println(ConsoleColors.RED + "Invalid booking number." + ConsoleColors.RESET);
            return;
        }
        Booking booking = currentPassenger.getBookings().get(bookingIndex);
        BookingComponent bookingComponent = booking.bookingComponent;
        
        double originalCost = booking.getCost();
        
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
        
        booking.setBookingComponent(bookingComponent);
        double newCost = booking.getCost();
        double additionalCost = newCost - originalCost;
        
        if (additionalCost > 0) {
            System.out.println(ConsoleColors.YELLOW + "Additional cost for services: $" + String.format("%.2f", additionalCost) + ConsoleColors.RESET);
            
            // Process payment for the additional services only if there are any
            if (paymentProcessor.processPayment(additionalCost, booking)) {
                // Add loyalty points for the additional purchase
                currentPassenger.addLoyaltyPoints((int) (additionalCost / 10));
                System.out.println(ConsoleColors.GREEN + "Final Booking: " + booking.getDescription() + " - Total Cost: $" + booking.getCost() + ConsoleColors.RESET);
            } else {
                // If payment fails, revert to original booking without the new services
                booking.setBookingComponent(bookingComponent);
                System.out.println(ConsoleColors.RED + "Customization cancelled due to payment failure. Original booking preserved." + ConsoleColors.RESET);
            }
        } else {
            System.out.println(ConsoleColors.GREEN + "Final Booking: " + booking.getDescription() + " - Total Cost: $" + booking.getCost() + ConsoleColors.RESET);
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
        System.out.print("Enter booking number: ");
        int bookingIndex = scanner.nextInt() - 1;
        scanner.nextLine();
        if (bookingIndex < 0 || bookingIndex >= currentPassenger.getBookings().size()) {
            System.out.println(ConsoleColors.RED + "Invalid booking number." + ConsoleColors.RESET);
            return;
        }
        Booking booking = currentPassenger.getBookings().get(bookingIndex);
        
        System.out.println(ConsoleColors.YELLOW + "Are you sure you want to cancel this booking? (y/n)" + ConsoleColors.RESET);
        String confirm = scanner.nextLine().toLowerCase();
        
        if (confirm.equals("y")) {
            currentPassenger.cancelBooking(booking);
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
}
