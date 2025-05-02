import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Random;
import java.util.Scanner;

public class PaymentProcessor {
    private Scanner scanner;
    
    public PaymentProcessor(Scanner scanner) {
        this.scanner = scanner;
    }
    
    public boolean processPayment(double amount, Booking booking) {
        System.out.println(ConsoleColors.CYAN + "\n--- Payment Processing ---" + ConsoleColors.RESET);
        System.out.println("Total amount to pay: $" + String.format("%.2f", amount));
        
        // Payment method selection
        System.out.println("\nSelect payment method:");
        System.out.println("1. Credit Card");
        System.out.println("2. PayPal");
        System.out.println("3. Bank Transfer");
        System.out.print("Choose an option: ");
        
        int paymentMethodChoice;
        try {
            paymentMethodChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Invalid input. Defaulting to Credit Card." + ConsoleColors.RESET);
            scanner.nextLine(); // Consume invalid input
            paymentMethodChoice = 1;
        }
        
        String paymentMethod;
        switch (paymentMethodChoice) {
            case 2: paymentMethod = "PayPal"; break;
            case 3: paymentMethod = "Bank Transfer"; break;
            default: paymentMethod = "Credit Card";
        }
        
        System.out.println("Selected payment method: " + paymentMethod);
        
        // For credit card, collect details
        if (paymentMethod.equals("Credit Card")) {
            if (!collectCreditCardDetails()) {
                return false;
            }
        } else if (paymentMethod.equals("PayPal")) {
            if (!collectPayPalDetails()) {
                return false;
            }
        } else { // Bank Transfer
            if (!collectBankDetails()) {
                return false;
            }
        }
        
        // Process payment
        System.out.println(ConsoleColors.YELLOW + "Processing payment..." + ConsoleColors.RESET);
        
        // Simulate payment processing delay
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Generate a random success (90% chance of success)
        boolean success = new Random().nextInt(100) < 90;
        
        if (success) {
            String receiptNumber = generateReceiptNumber();
            System.out.println(ConsoleColors.GREEN + "Payment successful!" + ConsoleColors.RESET);
            System.out.println("Receipt number: " + receiptNumber);
            System.out.println("Booking confirmed for: " + booking.getDescription());
            System.out.println("Thank you for your purchase!");
            return true;
        } else {
            System.out.println(ConsoleColors.RED + "Payment failed. Please try again." + ConsoleColors.RESET);
            return false;
        }
    }
    
    private boolean collectCreditCardDetails() {
        System.out.print("Enter credit card number (16 digits): ");
        String cardNumber = scanner.nextLine().replaceAll("\\s+", "");
        
        if (!cardNumber.matches("\\d{16}")) {
            System.out.println(ConsoleColors.RED + "Invalid card number. Must be 16 digits." + ConsoleColors.RESET);
            return false;
        }
        
        System.out.print("Enter expiration date (MM/YY): ");
        String expDate = scanner.nextLine();
        
        if (!expDate.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            System.out.println(ConsoleColors.RED + "Invalid expiration date. Must be in format MM/YY." + ConsoleColors.RESET);
            return false;
        }
        
        // Check if card is expired
        try {
            String[] parts = expDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000; // Convert to 4-digit year
            
            LocalDate expiryDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
            if (expiryDate.isBefore(LocalDate.now())) {
                System.out.println(ConsoleColors.RED + "Card is expired." + ConsoleColors.RESET);
                return false;
            }
        } catch (DateTimeParseException e) {
            System.out.println(ConsoleColors.RED + "Invalid date format." + ConsoleColors.RESET);
            return false;
        }
        
        System.out.print("Enter CVV (3 digits): ");
        String cvv = scanner.nextLine();
        
        if (!cvv.matches("\\d{3}")) {
            System.out.println(ConsoleColors.RED + "Invalid CVV. Must be 3 digits." + ConsoleColors.RESET);
            return false;
        }
        
        System.out.print("Enter cardholder name: ");
        String cardholderName = scanner.nextLine();
        
        if (cardholderName.isEmpty()) {
            System.out.println(ConsoleColors.RED + "Cardholder name cannot be empty." + ConsoleColors.RESET);
            return false;
        }
        
        return true;
    }
    
    private boolean collectPayPalDetails() {
        System.out.print("Enter PayPal email: ");
        String email = scanner.nextLine();
        
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            System.out.println(ConsoleColors.RED + "Invalid email format." + ConsoleColors.RESET);
            return false;
        }
        
        System.out.print("Enter PayPal password: ");
        String password = scanner.nextLine();
        
        if (password.length() < 6) {
            System.out.println(ConsoleColors.RED + "Password must be at least 6 characters." + ConsoleColors.RESET);
            return false;
        }
        
        return true;
    }
    
    private boolean collectBankDetails() {
        System.out.print("Enter bank account number: ");
        String accountNumber = scanner.nextLine().replaceAll("\\s+", "");
        
        if (!accountNumber.matches("\\d{8,20}")) {
            System.out.println(ConsoleColors.RED + "Invalid account number." + ConsoleColors.RESET);
            return false;
        }
        
        System.out.print("Enter bank routing number: ");
        String routingNumber = scanner.nextLine().replaceAll("\\s+", "");
        
        if (!routingNumber.matches("\\d{9}")) {
            System.out.println(ConsoleColors.RED + "Invalid routing number. Must be 9 digits." + ConsoleColors.RESET);
            return false;
        }
        
        return true;
    }
    
    private String generateReceiptNumber() {
        // Generate a random receipt number
        Random random = new Random();
        StringBuilder receiptBuilder = new StringBuilder("RCT-");
        for (int i = 0; i < 8; i++) {
            receiptBuilder.append(random.nextInt(10));
        }
        return receiptBuilder.toString();
    }
} 