import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class HospitalManagementSystem {
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_db";
    private static final String USER = "root";
    private static final String PASSWORD = "p@rth@2006";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);

            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);

            while (true) {
                System.out.println("=========================================");
                System.out.println("       HOSPITAL MANAGEMENT SYSTEM        ");
                System.out.println("=========================================");
                System.out.println("1. Register New Patient");
                System.out.println("2. View Patients");
                System.out.println("3. View Doctors");
                System.out.println("4. Book Appointment");
                System.out.println("5. View All Appointments");
                System.out.println("6. Exit");
                System.out.print("Enter your choice (1-6): ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input! Please enter a number from 1 to 6.\n");
                    scanner.next();
                    continue;
                }

                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear buffer

                switch (choice) {
                    case 1:
                        patient.addPatient();
                        break;
                    case 2:
                        patient.viewPatients();
                        break;
                    case 3:
                        doctor.viewDoctors();
                        break;
                    case 4:
                        bookAppointment(patient, doctor, connection, scanner);
                        break;
                    case 5:
                        viewAppointments(connection);
                        break;
                    case 6:
                        System.out.println("Closing database connection and exiting system. Goodbye!");
                        connection.close();
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid option. Please enter 1-6.\n");
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error: MySQL Connector JAR is not in the classpath!");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    public static void bookAppointment(Patient patient, Doctor doctor, Connection connection, Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid Patient ID number: ");
            scanner.next();
        }
        int patientId = scanner.nextInt();

        System.out.print("Enter Doctor ID: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid Doctor ID number: ");
            scanner.next();
        }
        int doctorId = scanner.nextInt();
        scanner.nextLine(); // Clear buffer

        System.out.print("Enter Appointment Date (YYYY-MM-DD): ");
        String date = scanner.nextLine().trim();

        // Validation Checks
        if (!patient.getPatientById(patientId)) {
            System.out.println(">> Error: Patient with ID " + patientId + " does not exist.\n");
            return;
        }

        if (!doctor.getDoctorById(doctorId)) {
            System.out.println(">> Error: Doctor with ID " + doctorId + " does not exist.\n");
            return;
        }

        // Check if doctor is already booked on this date
        String checkQuery = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, doctorId);
            checkStmt.setString(2, date);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println(">> Notice: Doctor is already booked on " + date + ". Please select another date.\n");
                    return;
                }
            }

            // Insert confirmed booking
            String insertQuery = "INSERT INTO appointments (patient_id, doctor_id, appointment_date) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, patientId);
                insertStmt.setInt(2, doctorId);
                insertStmt.setString(3, date);

                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    System.out.println(">> Appointment successfully booked!\n");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error during booking: " + e.getMessage());
        }
    }

    public static void viewAppointments(Connection connection) {
        String query = "SELECT a.id, p.name AS patient_name, d.name AS doctor_name, a.appointment_date " +
                       "FROM appointments a " +
                       "JOIN patients p ON a.patient_id = p.id " +
                       "JOIN doctors d ON a.doctor_id = d.id";

        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n--------------------------- SCHEDULED APPOINTMENTS ---------------------------");
            System.out.printf("%-5s %-20s %-20s %-12s\n", "ID", "Patient Name", "Doctor Name", "Date");
            System.out.println("------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-20s %-20s %-12s\n",
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getDate("appointment_date"));
            }
            if (!found) {
                System.out.println("No appointments scheduled currently.");
            }
            System.out.println("------------------------------------------------------------------------------\n");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }
}
