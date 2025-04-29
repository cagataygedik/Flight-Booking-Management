# Flight Booking Management System

A Java-based console application for flight booking management that implements several design patterns. The system allows users to search for flights, book tickets, customize bookings with additional services, view bookings, check flight status, and manage loyalty points.

## Features

- **User Authentication**: Login and registration system with admin privileges
- **Flight Search**: Search for flights by departure and arrival locations
- **Booking Management**: Create, view, and cancel flight bookings
- **Booking Customization**: Add services like insurance, meals, and priority boarding
- **Flight Status Notifications**: Subscribe to flight status updates
- **Admin Panel**: Manage flights, update statuses, add special offers, view statistics
- **Payment Processing**: Multiple payment methods with validation
- **Loyalty Program**: Earn points with purchases and track progress through tiers

## Design Patterns

This project implements several design patterns:

1. **Decorator Pattern**: Used for adding booking services (insurance, meals, priority boarding)
2. **Observer Pattern**: Notifies passengers of flight status changes
3. **Composite Pattern**: Used for building complex booking objects

## How to Run

1. Compile the Java files:
   ```
   javac -d bin src/*.java
   ```

2. Run the application:
   ```
   java -cp bin Main
   ```

## User Accounts

The system comes with default accounts:

- **Admin**: Username: `admin`, Password: `admin123`
- **Sample Users**: 
  - Username: `john`, Password: `pass123`
  - Username: `mary`, Password: `pass456`

## Project Structure

- `src/`: Source code files
- `data/`: Data files including flight information
- `bin/`: Compiled class files

## Testing

The system includes sample data to explore functionality. You can:
1. Login with an existing account or register a new one
2. Search for flights between available cities
3. Book flights and customize them with additional services
4. View your bookings and loyalty points
5. Access admin features with the admin account

## Dependencies

- Java 8 or higher
