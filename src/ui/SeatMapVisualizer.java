package ui;

import core.SeatMap;
import java.util.Scanner;

/**
 * Provides a console-based visualization of the aircraft seat map.
 */
public class SeatMapVisualizer {
    private final SeatMap seatMap;
    
    public SeatMapVisualizer(SeatMap seatMap) {
        this.seatMap = seatMap;
    }
    
    /**
     * Displays the seat map in the console with color coding.
     * - Green: Available seat
     * - Red: Occupied seat
     */
    public void displaySeatMap() {
        System.out.println(ConsoleColors.CYAN + "\n=== Seat Map for Flight " + seatMap.getFlightNumber() + " ===" + ConsoleColors.RESET);
        
        // Print column headers
        System.out.print("    ");
        for (int col = 0; col < seatMap.getColumns(); col++) {
            if (col == 3) {
                // Skip the aisle in column headers
                System.out.print("  ");
            }
            System.out.print(" " + (char)('A' + (col <= 2 ? col : col + 1)) + " ");
        }
        System.out.println();
        
        // Print rows
        for (int row = 1; row <= seatMap.getRows(); row++) {
            // Row number
            System.out.printf("%2d  ", row);
            
            // Seats in this row
            for (int col = 0; col < seatMap.getColumns(); col++) {
                String seatCode = seatMap.getSeatCode(row, col);
                
                // Add space for aisle after column C
                if (col == 3) {
                    System.out.print("| ");
                }
                
                // Color code: red for occupied, green for available
                if (seatMap.isSeatOccupied(seatCode)) {
                    System.out.print(ConsoleColors.RED + "[X]" + ConsoleColors.RESET);
                } else {
                    System.out.print(ConsoleColors.GREEN + "[ ]" + ConsoleColors.RESET);
                }
            }
            System.out.println();
            
            // Add separator every 5 rows
            if (row % 5 == 0 && row < seatMap.getRows()) {
                System.out.println("    " + "-".repeat(3 * seatMap.getColumns() + 2));
            }
        }
        
        System.out.println("\nLegend:");
        System.out.println(ConsoleColors.GREEN + "[ ]" + ConsoleColors.RESET + " Available");
        System.out.println(ConsoleColors.RED + "[X]" + ConsoleColors.RESET + " Occupied");
    }
    
    /**
     * Interactive seat selection process.
     * @param scanner Scanner for user input
     * @return Selected seat code or null if cancelled
     */
    public String selectSeat(Scanner scanner) {
        while (true) {
            displaySeatMap();
            
            System.out.println("\nEnter seat code (e.g., 15B) or 'exit' to cancel:");
            String input = scanner.nextLine().trim().toUpperCase();
            
            if (input.equalsIgnoreCase("exit")) {
                return null;
            }
            
            try {
                // Validate seat format
                int[] rowCol = seatMap.parseSeatCode(input);
                int row = rowCol[0];
                int col = rowCol[1];
                
                // Check if row and column are in valid range
                if (row < 1 || row > seatMap.getRows() || col < 0 || col >= seatMap.getColumns()) {
                    System.out.println(ConsoleColors.RED + "Invalid seat. Please choose a seat within the aircraft." + ConsoleColors.RESET);
                    continue;
                }
                
                // Check if seat is already occupied
                if (seatMap.isSeatOccupied(input)) {
                    System.out.println(ConsoleColors.RED + "Seat " + input + " is already occupied. Please choose another seat." + ConsoleColors.RESET);
                    continue;
                }
                
                // Confirm seat selection
                System.out.println(ConsoleColors.YELLOW + "You've selected seat " + input + ". Confirm? (y/n)" + ConsoleColors.RESET);
                String confirm = scanner.nextLine().trim().toLowerCase();
                
                if (confirm.equals("y") || confirm.equals("yes")) {
                    return input;
                }
            } catch (Exception e) {
                System.out.println(ConsoleColors.RED + "Invalid seat format. Please use format like '15B'." + ConsoleColors.RESET);
            }
        }
    }
} 