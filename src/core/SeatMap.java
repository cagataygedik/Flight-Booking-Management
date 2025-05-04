package core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SeatMap implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    private final int rows;
    private final int columns;
    
    
    private static final int DEFAULT_ROWS = 30;
    private static final int DEFAULT_COLUMNS = 7; 
    
    
    private Map<String, Boolean> occupiedSeats;
    
    
    private final String flightNumber;
    
    public SeatMap(String flightNumber) {
        this(flightNumber, DEFAULT_ROWS, DEFAULT_COLUMNS);
    }
    
    public SeatMap(String flightNumber, int rows, int columns) {
        this.flightNumber = flightNumber;
        this.rows = rows;
        this.columns = columns;
        this.occupiedSeats = new HashMap<>();
        
        
        initializeRandomOccupiedSeats();
    }
    
    private void initializeRandomOccupiedSeats() {
        Random random = new Random();
        
        int totalSeats = rows * columns;
        int seatsToOccupy = totalSeats * 30 / 100;
        
        for (int i = 0; i < seatsToOccupy; i++) {
            int randomRow = random.nextInt(rows) + 1; 
            int randomColumn = random.nextInt(columns); 
            
            String seatCode = getSeatCode(randomRow, randomColumn);
            
            
            if (!occupiedSeats.containsKey(seatCode)) {
                occupiedSeats.put(seatCode, true);
            } else {
                
                i--;
            }
        }
    }
    
    public String getSeatCode(int row, int column) {
        
        
        char columnLetter;
        if (column <= 2) {
            
            columnLetter = (char)('A' + column);
        } else {
            
            
            columnLetter = (char)('A' + column + 1);
        }
        
        return row + String.valueOf(columnLetter);
    }
    
    public int[] parseSeatCode(String seatCode) {
        
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
        
        
        int column;
        if (columnLetter >= 'A' && columnLetter <= 'C') {
            column = columnLetter - 'A';
        } else {
            column = columnLetter - 'A' - 1;
        }
        
        return new int[]{row, column};
    }
    
    public boolean bookSeat(String seatCode) {
        if (isSeatOccupied(seatCode)) {
            return false;
        }
        occupiedSeats.put(seatCode, true);
        return true;
    }
    
    public void releaseSeat(String seatCode) {
        occupiedSeats.remove(seatCode);
    }
    
    public boolean isSeatOccupied(String seatCode) {
        return occupiedSeats.containsKey(seatCode) && occupiedSeats.get(seatCode);
    }
    
    public boolean isSeatAvailable(String seatCode) {
        return !isSeatOccupied(seatCode);
    }
    
    public boolean changeSeat(String seatCode) {
        
        if (!isSeatAvailable(seatCode)) {
            return false;
        }
        
        
        return bookSeat(seatCode);
    }
    
    public Map<String, Boolean> getOccupiedSeats() {
        return new HashMap<>(occupiedSeats);
    }
    
    public int getRows() {
        return rows;
    }
    
    public int getColumns() {
        return columns;
    }
    
    public String getFlightNumber() {
        return flightNumber;
    }
    
    public String getNextAvailableSeat() {
        
        for (int row = 1; row <= rows; row++) {
            for (int col = 0; col < columns; col++) {
                String seatCode = getSeatCode(row, col);
                if (!isSeatOccupied(seatCode)) {
                    return seatCode;
                }
            }
        }
        
        
        return null;
    }
} 