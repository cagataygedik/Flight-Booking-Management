package auth;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.nio.file.*;

import java.io.Serializable;

import core.Passenger;
import ui.ConsoleColors;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private Passenger passenger;
    private boolean isAdmin;
    private static List<User> users = new ArrayList<>();
    private static final String USERS_FILE = "data/users.dat";
    
    static {
        loadUsers();
        
        if (users.isEmpty()) {
            User admin = new User("admin", "admin123", "Admin", true);
            users.add(admin);
            
            users.add(new User("john", "pass123", "John Smith", false));
            users.add(new User("mary", "pass456", "Mary Johnson", false));
            
            saveUsers();
        }
    }
    
    public User(String username, String password, String passengerName, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.passenger = new Passenger(passengerName);
        this.isAdmin = isAdmin;
    }
    
    public static User authenticate(String username, String password) {
        for (User user : users) {
            if (user.username.equals(username) && user.password.equals(password)) {
                return user;
            }
        }
        return null;
    }
    
    public static void register(String username, String password, String passengerName) {
        for (User user : users) {
            if (user.username.equals(username)) {
                throw new IllegalArgumentException("Username already exists");
            }
        }
        
        User newUser = new User(username, password, passengerName, false);
        users.add(newUser);
        saveUsers();
    }
    
    private static void saveUsers() {
        try {
            Files.createDirectories(Paths.get("data"));
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
                oos.writeObject(users);
                System.out.println(ConsoleColors.GREEN + "Users saved successfully." + ConsoleColors.RESET);
            }
        } catch (IOException e) {
            System.err.println(ConsoleColors.RED + "Error saving users: " + e.getMessage() + ConsoleColors.RESET);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            System.out.println("No users file found. Starting with default users.");
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            users = (List<User>) ois.readObject();
            System.out.println(ConsoleColors.GREEN + "Loaded " + users.size() + " users." + ConsoleColors.RESET);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(ConsoleColors.RED + "Error loading users: " + e.getMessage() + ConsoleColors.RESET);
            users = new ArrayList<>();
        }
    }
    
    public String getUsername() {
        return username;
    }
    
    public Passenger getPassenger() {
        return passenger;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
} 