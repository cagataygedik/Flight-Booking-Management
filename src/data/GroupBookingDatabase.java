package data;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDate;

import core.GroupBooking;
import core.Passenger;
import ui.ConsoleColors;

/**
 * Handles persistence of group bookings.
 */
public class GroupBookingDatabase {
    private List<GroupBooking> groupBookings = new ArrayList<>();
    private static final String GROUP_BOOKINGS_FILE = "data/group_bookings.dat";
    
    /**
     * Constructs a new GroupBookingDatabase and loads existing data.
     */
    public GroupBookingDatabase() {
        loadGroupBookings();
    }
    
    /**
     * Adds a new group booking and saves to file.
     * 
     * @param groupBooking The group booking to add
     * @return true if added successfully
     */
    public boolean addGroupBooking(GroupBooking groupBooking) {
        // Check if group ID already exists
        for (GroupBooking existing : groupBookings) {
            if (existing.getGroupId().equals(groupBooking.getGroupId())) {
                return false;
            }
        }
        
        groupBookings.add(groupBooking);
        saveGroupBookings();
        return true;
    }
    
    /**
     * Removes a group booking by its ID.
     * 
     * @param groupId The ID of the group booking to remove
     * @return true if removed successfully
     */
    public boolean removeGroupBooking(String groupId) {
        for (Iterator<GroupBooking> it = groupBookings.iterator(); it.hasNext();) {
            GroupBooking booking = it.next();
            if (booking.getGroupId().equals(groupId)) {
                it.remove();
                saveGroupBookings();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Finds a group booking by its ID.
     * 
     * @param groupId The ID of the group booking to find
     * @return The group booking or null if not found
     */
    public GroupBooking getGroupBookingById(String groupId) {
        for (GroupBooking booking : groupBookings) {
            if (booking.getGroupId().equals(groupId)) {
                return booking;
            }
        }
        return null;
    }
    
    /**
     * Gets all group bookings for a specific passenger.
     * 
     * @param passenger The passenger to find bookings for
     * @return List of group bookings containing the passenger
     */
    public List<GroupBooking> getGroupBookingsForPassenger(Passenger passenger) {
        List<GroupBooking> result = new ArrayList<>();
        for (GroupBooking booking : groupBookings) {
            if (booking.getPassengers().contains(passenger)) {
                result.add(booking);
            }
        }
        return result;
    }
    
    /**
     * Gets all group bookings.
     * 
     * @return List of all group bookings
     */
    public List<GroupBooking> getAllGroupBookings() {
        return new ArrayList<>(groupBookings);
    }
    
    /**
     * Generates a unique group ID.
     * 
     * @return A new unique group ID
     */
    public String generateGroupId() {
        String prefix = "GRP-";
        Random random = new Random();
        String groupId;
        
        do {
            StringBuilder idBuilder = new StringBuilder(prefix);
            for (int i = 0; i < 6; i++) {
                idBuilder.append(random.nextInt(10));
            }
            groupId = idBuilder.toString();
        } while (getGroupBookingById(groupId) != null);
        
        return groupId;
    }
    
    /**
     * Updates an existing group booking in the database.
     * 
     * @param groupBooking The updated group booking
     * @return true if updated successfully
     */
    public boolean updateGroupBooking(GroupBooking groupBooking) {
        // Check if the group booking exists
        boolean found = false;
        for (int i = 0; i < groupBookings.size(); i++) {
            if (groupBookings.get(i).getGroupId().equals(groupBooking.getGroupId())) {
                groupBookings.set(i, groupBooking);
                found = true;
                break;
            }
        }
        
        if (!found) {
            return false;
        }
        
        // Save the updated list
        saveGroupBookings();
        return true;
    }
    
    /**
     * Saves group bookings to file.
     */
    private void saveGroupBookings() {
        try {
            // Ensure the directory exists
            Files.createDirectories(Paths.get("data"));
            
            // Save group bookings to file
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GROUP_BOOKINGS_FILE))) {
                oos.writeObject(groupBookings);
                System.out.println(ConsoleColors.GREEN + "Group bookings saved successfully." + ConsoleColors.RESET);
            }
        } catch (IOException e) {
            System.err.println(ConsoleColors.RED + "Error saving group bookings: " + e.getMessage() + ConsoleColors.RESET);
        }
    }
    
    /**
     * Loads group bookings from file.
     */
    @SuppressWarnings("unchecked")
    private void loadGroupBookings() {
        File file = new File(GROUP_BOOKINGS_FILE);
        if (!file.exists()) {
            System.out.println("No group bookings file found. Starting with empty database.");
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(GROUP_BOOKINGS_FILE))) {
            groupBookings = (List<GroupBooking>) ois.readObject();
            System.out.println(ConsoleColors.GREEN + "Loaded " + groupBookings.size() + " group bookings." + ConsoleColors.RESET);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(ConsoleColors.RED + "Error loading group bookings: " + e.getMessage() + ConsoleColors.RESET);
            // If loading fails, start with an empty list
            groupBookings = new ArrayList<>();
        }
    }
} 