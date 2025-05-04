package ui;

import java.util.ArrayList;
import java.util.List;

public class TableFormatter {
    
    private List<String[]> rows = new ArrayList<>();
    private String[] headers;
    private int[] columnWidths;
    private boolean useColors = true;
    
    public TableFormatter(String... headers) {
        this.headers = headers;
        this.columnWidths = new int[headers.length];
        
        
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }
    }
    
    public void addRow(String... values) {
        String[] row = new String[headers.length];
        for (int i = 0; i < Math.min(values.length, headers.length); i++) {
            row[i] = values[i];
            
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
        
        
        result.append(line).append("\n");
        
        
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
        
        
        result.append(line).append("\n");
        
        
        for (String[] row : rows) {
            result.append("| ");
            for (int i = 0; i < headers.length; i++) {
                String value = row[i] != null ? row[i] : "";
                String paddedValue = value + " ".repeat(Math.max(0, columnWidths[i] - value.length()));
                result.append(paddedValue).append(" | ");
            }
            result.append("\n");
        }
        
        
        result.append(line);
        
        return result.toString();
    }
}
