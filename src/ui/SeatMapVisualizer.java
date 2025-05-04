package ui;

import core.SeatMap;
import java.util.Scanner;
import java.util.Map;

public class SeatMapVisualizer {
    private SeatMap seatMap;
    
    public SeatMapVisualizer(SeatMap seatMap) {
        this.seatMap = seatMap;
    }
    
    public String selectSeat(Scanner scanner) {
        
        displaySeatMap();
        
        
        System.out.println(ConsoleColors.CYAN + "Enter seat code (e.g., 15B) or 'cancel' to go back:" + ConsoleColors.RESET);
        String seatCode = scanner.nextLine().toUpperCase();
        
        if (seatCode.equalsIgnoreCase("cancel")) {
            return null;
        }
        
        
        try {
            int[] seatPosition = seatMap.parseSeatCode(seatCode);
            int row = seatPosition[0];
            int column = seatPosition[1];
            
            if (row < 1 || row > seatMap.getRows() || column < 0 || column >= seatMap.getColumns()) {
                System.out.println(ConsoleColors.RED + "Invalid seat code. Please try again." + ConsoleColors.RESET);
                return selectSeat(scanner);
            }
            
            
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
    
    private void displaySeatMap() {
        Map<String, Boolean> occupiedSeats = seatMap.getOccupiedSeats();
        
        System.out.println(ConsoleColors.CYAN + "\n--- Seat Map for Flight " + seatMap.getFlightNumber() + " ---" + ConsoleColors.RESET);
        System.out.println("      A   B   C       D   E   F");
        System.out.println("    +---+---+---+   +---+---+---+");
        
        for (int row = 1; row <= seatMap.getRows(); row++) {
            
            String rowStr = Integer.toString(row);
            if (rowStr.length() == 1) {
                System.out.print(" ");
            }
            System.out.print(rowStr + " | ");
            
            
            for (int col = 0; col < seatMap.getColumns(); col++) {
                String seatCode = seatMap.getSeatCode(row, col);
                
                
                if (col == 3) {
                    System.out.print("  | ");
                }
                
                
                if (col != 3) {
                    if (occupiedSeats.containsKey(seatCode)) {
                        
                        System.out.print(ConsoleColors.RED + "X" + ConsoleColors.RESET + " | ");
                    } else {
                        
                        System.out.print(ConsoleColors.GREEN + "O" + ConsoleColors.RESET + " | ");
                    }
                }
            }
            System.out.println("\n    +---+---+---+   +---+---+---+");
            
            
            if (row % 10 == 0 && row < seatMap.getRows()) {
                System.out.println();
            }
        }
        
        System.out.println("\nLegend: " + ConsoleColors.GREEN + "O" + ConsoleColors.RESET + " = Available, " + 
                          ConsoleColors.RED + "X" + ConsoleColors.RESET + " = Occupied\n");
    }
} 