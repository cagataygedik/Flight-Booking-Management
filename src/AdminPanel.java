import java.util.List;
import java.util.Scanner;

public class AdminPanel {
    private final FlightDatabase flightDb;
    private final Scanner scanner;
    
    public AdminPanel(FlightDatabase flightDb, Scanner scanner) {
        this.flightDb = flightDb;
        this.scanner = scanner;
    }
    
    public void displayMenu() {
        System.out.println(ConsoleColors.CYAN + "\n--- Admin Panel ---" + ConsoleColors.RESET);
        System.out.println("1. View All Flights");
        System.out.println("2. Update Flight Status");
        System.out.println("3. Add Special Offer");
        System.out.println("4. View Statistics");
        System.out.println("5. Return to Main Menu");
        System.out.print("Choose an option: ");
    }
    
    public void run() {
        while (true) {
            displayMenu();
            int choice = getNumericChoice(1, 5);
            
            switch (choice) {
                case 1:
                    viewAllFlights();
                    break;
                case 2:
                    updateFlightStatus();
                    break;
                case 3:
                    addSpecialOffer();
                    break;
                case 4:
                    viewStatistics();
                    break;
                case 5:
                    return;
                default:
                    System.out.println(ConsoleColors.RED + "Invalid choice. Please try again." + ConsoleColors.RESET);
            }
        }
    }
    
    private void viewAllFlights() {
        List<Flight> allFlights = flightDb.getAllFlights();
        System.out.println(ConsoleColors.CYAN + "--- All Flights in System ---" + ConsoleColors.RESET);
        if (allFlights.isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "No flights found in the database." + ConsoleColors.RESET);
        } else {
            for (Flight flight : allFlights) {
                System.out.println(flight);
            }
            System.out.println(ConsoleColors.GREEN + "Total flights: " + allFlights.size() + ConsoleColors.RESET);
        }
    }
    
    private void updateFlightStatus() {
        System.out.print("Enter flight number to update: ");
        String flightNumber = scanner.nextLine();
        Flight flight = flightDb.getFlightByNumber(flightNumber);
        
        if (flight == null) {
            System.out.println(ConsoleColors.RED + "Flight not found." + ConsoleColors.RESET);
            return;
        }
        
        System.out.println("Current status: " + flight.getStatus());
        System.out.println("1. On Time");
        System.out.println("2. Delayed");
        System.out.println("3. Cancelled");
        System.out.println("4. Boarding");
        System.out.println("5. Departed");
        System.out.println("6. Landed");
        System.out.print("Select new status: ");
        
        int statusChoice = getNumericChoice(1, 6);
        String newStatus;
        
        switch (statusChoice) {
            case 1: newStatus = "On Time"; break;
            case 2: newStatus = "Delayed"; break;
            case 3: newStatus = "Cancelled"; break;
            case 4: newStatus = "Boarding"; break;
            case 5: newStatus = "Departed"; break;
            case 6: newStatus = "Landed"; break;
            default: newStatus = "On Time";
        }
        
        flight.setStatus(newStatus);
        System.out.println(ConsoleColors.GREEN + "Flight status updated to: " + newStatus + ConsoleColors.RESET);
    }
    
    private void addSpecialOffer() {
        System.out.print("Enter flight number for special offer: ");
        String flightNumber = scanner.nextLine();
        Flight flight = flightDb.getFlightByNumber(flightNumber);
        
        if (flight == null) {
            System.out.println(ConsoleColors.RED + "Flight not found." + ConsoleColors.RESET);
            return;
        }
        
        System.out.print("Enter discount percentage (1-50): ");
        int discount = getNumericChoice(1, 50);
        
        double originalPrice = flight.getPrice();
        double newPrice = originalPrice * (1 - (discount / 100.0));
        flight.setSpecialOffer(discount, newPrice);
        
        System.out.println(ConsoleColors.GREEN + "Special offer added: " + discount + "% off" + ConsoleColors.RESET);
        System.out.println("Original price: $" + originalPrice + " -> New price: $" + newPrice);
    }
    
    private void viewStatistics() {
        List<Flight> allFlights = flightDb.getAllFlights();
        
        int totalFlights = allFlights.size();
        int delayedFlights = 0;
        int cancelledFlights = 0;
        double totalRevenue = 0;
        
        for (Flight flight : allFlights) {
            if (flight.getStatus().equals("Delayed")) {
                delayedFlights++;
            } else if (flight.getStatus().equals("Cancelled")) {
                cancelledFlights++;
            }
            
            // Assume each flight has 100 seats and 70% occupancy rate for revenue calculation
            totalRevenue += flight.getPrice() * 70;
        }
        
        System.out.println(ConsoleColors.CYAN + "--- System Statistics ---" + ConsoleColors.RESET);
        System.out.println("Total Flights: " + totalFlights);
        System.out.println("Delayed Flights: " + delayedFlights + " (" + 
                           (totalFlights > 0 ? (delayedFlights * 100 / totalFlights) : 0) + "%)");
        System.out.println("Cancelled Flights: " + cancelledFlights + " (" + 
                           (totalFlights > 0 ? (cancelledFlights * 100 / totalFlights) : 0) + "%)");
        System.out.println("Estimated Revenue: $" + String.format("%.2f", totalRevenue));
    }
    
    private int getNumericChoice(int min, int max) {
        while (true) {
            try {
                System.out.print("Enter your choice (" + min + "-" + max + "): ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    System.out.println(ConsoleColors.RED + "Please enter a number between " + min + " and " + max + "." + ConsoleColors.RESET);
                }
            } catch (Exception e) {
                System.out.println(ConsoleColors.RED + "Invalid input. Please enter a number." + ConsoleColors.RESET);
                scanner.nextLine(); // Consume invalid input
            }
        }
    }
} 