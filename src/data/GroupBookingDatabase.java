package data;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDate;

import core.GroupBooking;
import core.Passenger;
import ui.ConsoleColors;

public class GroupBookingDatabase {
    private List<GroupBooking> groupBookings = new ArrayList<>();
    private static final String GROUP_BOOKINGS_FILE = "data/group_bookings.dat";
    
    public GroupBookingDatabase() {
        loadGroupBookings();
    }
    
    public boolean addGroupBooking(GroupBooking groupBooking) {
        
        for (GroupBooking existing : groupBookings) {
            if (existing.getGroupId().equals(groupBooking.getGroupId())) {
                return false;
            }
        }
        
        groupBookings.add(groupBooking);
        saveGroupBookings();
        return true;
    }
    
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
    
    public GroupBooking getGroupBookingById(String groupId) {
        for (GroupBooking booking : groupBookings) {
            if (booking.getGroupId().equals(groupId)) {
                return booking;
            }
        }
        return null;
    }
    
    public List<GroupBooking> getGroupBookingsForPassenger(Passenger passenger) {
        List<GroupBooking> result = new ArrayList<>();
        for (GroupBooking booking : groupBookings) {
            if (booking.getPassengers().contains(passenger)) {
                result.add(booking);
            }
        }
        return result;
    }
    
    public List<GroupBooking> getAllGroupBookings() {
        return new ArrayList<>(groupBookings);
    }
    
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
    
    public boolean updateGroupBooking(GroupBooking groupBooking) {
        
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
        
        
        saveGroupBookings();
        return true;
    }
    
    private void saveGroupBookings() {
        try {
            
            Files.createDirectories(Paths.get("data"));
            
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GROUP_BOOKINGS_FILE))) {
                oos.writeObject(groupBookings);
                System.out.println(ConsoleColors.GREEN + "Group bookings saved successfully." + ConsoleColors.RESET);
            }
        } catch (IOException e) {
            System.err.println(ConsoleColors.RED + "Error saving group bookings: " + e.getMessage() + ConsoleColors.RESET);
        }
    }
    
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
            
            groupBookings = new ArrayList<>();
        }
    }
} 