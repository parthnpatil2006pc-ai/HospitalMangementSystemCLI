import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Patient {
    private Connection connection;
    private Scanner scanner;

    public Patient(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void addPatient() {
        System.out.print("Enter Patient Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Patient Age: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid age! Please enter a valid number: ");
            scanner.next();
        }
        int age = scanner.nextInt();
        scanner.nextLine(); // Clear buffer

        System.out.print("Enter Gender (Male/Female/Other): ");
        String gender = scanner.nextLine();

        System.out.print("Enter Contact Number: ");
        String contact = scanner.nextLine();

        String query = "INSERT INTO patients (name, age, gender, contact) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            pstmt.setString(4, contact);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println(">> Patient registered successfully!\n");
            } else {
                System.out.println(">> Failed to register patient.\n");
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public void viewPatients() {
        String query = "SELECT * FROM patients";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n-------------------------- PATIENTS LIST --------------------------");
            System.out.printf("%-5s %-20s %-6s %-10s %-15s\n", "ID", "Name", "Age", "Gender", "Contact");
            System.out.println("-------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-20s %-6d %-10s %-15s\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("contact"));
            }
            if (!found) {
                System.out.println("No registered patients found.");
            }
            System.out.println("-------------------------------------------------------------------\n");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public boolean getPatientById(int id) {
        String query = "SELECT id FROM patients WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking patient: " + e.getMessage());
            return false;
        }
    }
}