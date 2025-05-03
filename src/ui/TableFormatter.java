package ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Created a new utility class for formatting tables in the console.
 */
public class TableFormatter {
    
    private List<String[]> rows = new ArrayList<>();
    private String[] headers;
    private int[] columnWidths;
    private boolean useColors = true;
    
    public TableFormatter(String... headers) {
        this.headers = headers;
        this.columnWidths = new int[headers.length];
        
        // Initialize columnWidths based on headers
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }
    }
    
    public void addRow(String... values) {
        String[] row = new String[headers.length];
        for (int i = 0; i < Math.min(values.length, headers.length); i++) {
            row[i] = values[i];
            // Update column width if this value is wider
            if (values[i].length() > columnWidths[i]) {
                columnWidths[i] = values[i].length();
            }
        }
        rows.add(row);
    }
    
    private String createLine() {
        StringBuilder line = new StringBuilder("+");
        for (int width : columnWidths) {
            line.append("-".repeat(width + 2)).append("+");
        }
        return line.toString();
    }
    
    public String render() {
        StringBuilder result = new StringBuilder();
        String line = createLine();
        
        // Add top line
        result.append(line).append("\n");
        
        // Add header
        result.append("| ");
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            String paddedHeader = header + " ".repeat(Math.max(0, columnWidths[i] - header.length()));
            if (useColors) {
                result.append(ConsoleColors.CYAN).append(paddedHeader).append(ConsoleColors.RESET);
            } else {
                result.append(paddedHeader);
            }
            result.append(" | ");
        }
        result.append("\n");
        
        // Add separator line
        result.append(line).append("\n");
        
        // Add rows
        for (String[] row : rows) {
            result.append("| ");
            for (int i = 0; i < headers.length; i++) {
                String value = row[i] != null ? row[i] : "";
                String paddedValue = value + " ".repeat(Math.max(0, columnWidths[i] - value.length()));
                result.append(paddedValue).append(" | ");
            }
            result.append("\n");
        }
        
        // Add bottom line
        result.append(line);
        
        return result.toString();
    }
}
