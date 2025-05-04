package core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Represents the seat map of a flight with information about seat availability.
 */
public class SeatMap implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Number of rows and columns in the aircraft
    private final int rows;
    private final int columns;
    
    // Standard configuration with 3 seats on each side and an aisle
    private static final int DEFAULT_ROWS = 30;
    private static final int DEFAULT_COLUMNS = 7; // A, B, C, [aisle], D, E, F
    
    // Map to track which seats are occupied
    private Map<String, Boolean> occupiedSeats;
    
    // Flight this seat map belongs to
    private final String flightNumber;
    
    /**
     * Creates a seat map for a flight with the default configuration.
     * @param flightNumber The flight number this seat map belongs to
     */
    public SeatMap(String flightNumber) {
        this(flightNumber, DEFAULT_ROWS, DEFAULT_COLUMNS);
    }
    
    /**
     * Creates a seat map for a flight with a custom configuration.
     * @param flightNumber The flight number this seat map belongs to
     * @param rows Number of rows in the aircraft
     * @param columns Number of columns (seats per row)
     */
    public SeatMap(String flightNumber, int rows, int columns) {
        this.flightNumber = flightNumber;
        this.rows = rows;
        this.columns = columns;
        this.occupiedSeats = new HashMap<>();
        
        // Initialize some random seats as occupied
        initializeRandomOccupiedSeats();
    }
    
    /**
     * Randomly marks some seats as occupied to simulate a partially filled flight.
     */
    private void initializeRandomOccupiedSeats() {
        Random random = new Random();
        // Occupy around 30% of seats randomly
        int totalSeats = rows * columns;
        int seatsToOccupy = totalSeats * 30 / 100;
        
        for (int i = 0; i < seatsToOccupy; i++) {
            int randomRow = random.nextInt(rows) + 1; // Rows are 1-based
            int randomColumn = random.nextInt(columns); // Columns are 0-based
            
            String seatCode = getSeatCode(randomRow, randomColumn);
            
            // Only occupy if not already occupied
            if (!occupiedSeats.containsKey(seatCode)) {
                occupiedSeats.put(seatCode, true);
            } else {
                // Try again if this seat is already occupied
                i--;
            }
        }
    }
    
    /**
     * Converts a seat position to a seat code (e.g., row 1, column 0 becomes "1A").
     * @param row Row number (1-based)
     * @param column Column number (0-based)
     * @return Seat code as string
     */
    public String getSeatCode(int row, int column) {
        // Convert column number to letter (A, B, C, D, E, F)
        // Skip 'I' and 'O' as they can be confused with numbers
        char columnLetter;
        if (column <= 2) {
            // A, B, C (left side)
            columnLetter = (char)('A' + column);
        } else {
            // D, E, F (right side)
            // Add 1 to skip the aisle
            columnLetter = (char)('A' + column + 1);
        }
        
        return row + String.valueOf(columnLetter);
    }
    
    /**
     * Parses a seat code to row and column indexes.
     * @param seatCode Seat code like "12B"
     * @return int array with [row, column]
     */
    public int[] parseSeatCode(String seatCode) {
        // Extract row number and column letter
        StringBuilder rowStr = new StringBuilder();
        char columnLetter = ' ';
        
        for (char c : seatCode.toCharArray()) {
            if (Character.isDigit(c)) {
                rowStr.append(c);
            } else {
                columnLetter = Character.toUpperCase(c);
                break;
            }
        }
        
        int row = Integer.parseInt(rowStr.toString());
        
        // Convert column letter to column index
        int column;
        if (columnLetter >= 'A' && columnLetter <= 'C') {
            column = columnLetter - 'A';
        } else {
            column = columnLetter - 'A' - 1;
        }
        
        return new int[]{row, column};
    }
    
    /**
     * Books a seat.
     * @param seatCode The seat to book (e.g., "12B")
     * @return true if booking was successful, false if seat is already occupied
     */
    public boolean bookSeat(String seatCode) {
        if (isSeatOccupied(seatCode)) {
            return false;
        }
        occupiedSeats.put(seatCode, true);
        return true;
    }
    
    /**
     * Releases a booked seat.
     * @param seatCode The seat to release
     */
    public void releaseSeat(String seatCode) {
        occupiedSeats.remove(seatCode);
    }
    
    /**
     * Checks if a seat is occupied.
     * @param seatCode The seat to check
     * @return true if the seat is occupied, false otherwise
     */
    public boolean isSeatOccupied(String seatCode) {
        return occupiedSeats.containsKey(seatCode) && occupiedSeats.get(seatCode);
    }
    
    /**
     * Checks if a seat is available (not occupied).
     * @param seatCode The seat to check
     * @return true if the seat is available, false if it's occupied
     */
    public boolean isSeatAvailable(String seatCode) {
        return !isSeatOccupied(seatCode);
    }
    
    /**
     * Changes the seat assignment.
     * @param seatCode The new seat to assign
     * @return true if seat change was successful, false otherwise
     */
    public boolean changeSeat(String seatCode) {
        // First check if the seat is available
        if (!isSeatAvailable(seatCode)) {
            return false;
        }
        
        // Book the new seat
        return bookSeat(seatCode);
    }
    
    /**
     * Gets all occupied seats.
     * @return Map of occupied seats
     */
    public Map<String, Boolean> getOccupiedSeats() {
        return new HashMap<>(occupiedSeats);
    }
    
    /**
     * Gets the number of rows in the aircraft.
     * @return Number of rows
     */
    public int getRows() {
        return rows;
    }
    
    /**
     * Gets the number of columns in the aircraft.
     * @return Number of columns
     */
    public int getColumns() {
        return columns;
    }
    
    /**
     * Gets the flight number.
     * @return Flight number
     */
    public String getFlightNumber() {
        return flightNumber;
    }
    
    /**
     * Finds the next available seat in the aircraft.
     * Searches row by row starting from the front.
     * 
     * @return The seat code of the next available seat, or null if the flight is full
     */
    public String getNextAvailableSeat() {
        // Look for an available seat row by row
        for (int row = 1; row <= rows; row++) {
            for (int col = 0; col < columns; col++) {
                String seatCode = getSeatCode(row, col);
                if (!isSeatOccupied(seatCode)) {
                    return seatCode;
                }
            }
        }
        
        // If no seat is available, return null (flight is full)
        return null;
    }
} 