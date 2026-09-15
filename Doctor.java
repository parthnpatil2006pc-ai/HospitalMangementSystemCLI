import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Doctor {
    private Connection connection;

    public Doctor(Connection connection) {
        this.connection = connection;
    }

    public void viewDoctors() {
        String query = "SELECT * FROM doctors";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n----------------------- DOCTORS LIST -----------------------");
            System.out.printf("%-5s %-25s %-20s\n", "ID", "Doctor Name", "Specialization");
            System.out.println("------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-25s %-20s\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialization"));
            }
            if (!found) {
                System.out.println("No doctors available in records.");
            }
            System.out.println("------------------------------------------------------------\n");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public boolean getDoctorById(int id) {
        String query = "SELECT id FROM doctors WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking doctor: " + e.getMessage());
            return false;
        }
    }
}