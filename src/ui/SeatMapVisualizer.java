package ui;

import core.SeatMap;
import java.util.Scanner;
import java.util.Map;

/**
 * Provides a visual representation of the seat map and allows interactive selection of seats.
 */
public class SeatMapVisualizer {
    private SeatMap seatMap;
    
    /**
     * Creates a new seat map visualizer.
     * @param seatMap The seat map to visualize
     */
    public SeatMapVisualizer(SeatMap seatMap) {
        this.seatMap = seatMap;
    }
    
    /**
     * Displays the seat map visually and allows the user to select a seat.
     * @param scanner Scanner for user input
     * @return The selected seat code or null if selection was cancelled
     */
    public String selectSeat(Scanner scanner) {
        // Display the seat map
        displaySeatMap();
        
        // Prompt the user to select a seat
        System.out.println(ConsoleColors.CYAN + "Enter seat code (e.g., 15B) or 'cancel' to go back:" + ConsoleColors.RESET);
        String seatCode = scanner.nextLine().toUpperCase();
        
        if (seatCode.equalsIgnoreCase("cancel")) {
            return null;
        }
        
        // Validate the seat code
        try {
            int[] seatPosition = seatMap.parseSeatCode(seatCode);
            int row = seatPosition[0];
            int column = seatPosition[1];
            
            if (row < 1 || row > seatMap.getRows() || column < 0 || column >= seatMap.getColumns()) {
                System.out.println(ConsoleColors.RED + "Invalid seat code. Please try again." + ConsoleColors.RESET);
                return selectSeat(scanner);
            }
            
            // Check if the seat is occupied
            if (seatMap.isSeatOccupied(seatCode)) {
                System.out.println(ConsoleColors.RED + "This seat is already occupied. Please select another seat." + ConsoleColors.RESET);
                return selectSeat(scanner);
            }
            
            return seatCode;
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Invalid seat code format. Please enter a valid seat code (e.g., 15B)." + ConsoleColors.RESET);
            return selectSeat(scanner);
        }
    }
    
    /**
     * Displays a visual representation of the seat map.
     */
    private void displaySeatMap() {
        Map<String, Boolean> occupiedSeats = seatMap.getOccupiedSeats();
        
        System.out.println(ConsoleColors.CYAN + "\n--- Seat Map for Flight " + seatMap.getFlightNumber() + " ---" + ConsoleColors.RESET);
        System.out.println("      A   B   C       D   E   F");
        System.out.println("    +---+---+---+   +---+---+---+");
        
        for (int row = 1; row <= seatMap.getRows(); row++) {
            // Format row number with proper alignment
            String rowStr = Integer.toString(row);
            if (rowStr.length() == 1) {
                System.out.print(" ");
            }
            System.out.print(rowStr + " | ");
            
            // Print seats for each row
            for (int col = 0; col < seatMap.getColumns(); col++) {
                String seatCode = seatMap.getSeatCode(row, col);
                
                // Add aisle separator after 3rd seat
                if (col == 3) {
                    System.out.print("  | ");
                }
                
                // Skip the aisle column
                if (col != 3) {
                    if (occupiedSeats.containsKey(seatCode)) {
                        // Occupied seat
                        System.out.print(ConsoleColors.RED + "X" + ConsoleColors.RESET + " | ");
                    } else {
                        // Available seat
                        System.out.print(ConsoleColors.GREEN + "O" + ConsoleColors.RESET + " | ");
                    }
                }
            }
            System.out.println("\n    +---+---+---+   +---+---+---+");
            
            // Add a separator every 10 rows for better readability
            if (row % 10 == 0 && row < seatMap.getRows()) {
                System.out.println();
            }
        }
        
        System.out.println("\nLegend: " + ConsoleColors.GREEN + "O" + ConsoleColors.RESET + " = Available, " + 
                          ConsoleColors.RED + "X" + ConsoleColors.RESET + " = Occupied\n");
    }
} 