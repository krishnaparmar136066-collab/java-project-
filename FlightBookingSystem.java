import java.sql.*;
import java.util.Scanner;

/**
 * Flight Booking System - Java console application.
 * Configure the DATABASE_URL, USERNAME and PASSWORD before running.
 */
public class FlightBookingSystem {
    private static final String DATABASE_URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USERNAME = "flight_user";
    private static final String PASSWORD = "flight123";
    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD)) {
            System.out.println("\n===== FLIGHT BOOKING SYSTEM =====");
            int choice;
            do {
                printMenu();
                choice = readInt("Enter choice: ");
                switch (choice) {
                    case 1: viewFlights(connection); break;
                    case 2: searchFlights(connection); break;
                    case 3: bookFlight(connection); break;
                    case 4: viewBookings(connection); break;
                    case 5: cancelBooking(connection); break;
                    case 0: System.out.println("Thank you for using the system."); break;
                    default: System.out.println("Invalid choice. Try again.");
                }
            } while (choice != 0);
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            System.out.println("Check Oracle XE, JDBC driver, and database credentials.");
        }
    }

    private static void printMenu() {
        System.out.println("\n1. View all flights\n2. Search flight\n3. Book flight\n4. View bookings\n5. Cancel booking\n0. Exit");
    }

    private static void viewFlights(Connection con) throws SQLException {
        String sql = "SELECT flight_id, flight_no, source, destination, departure_time, price, available_seats FROM flights ORDER BY flight_id";
        try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            System.out.printf("%-4s %-10s %-15s %-15s %-20s %-10s %-6s%n", "ID", "Flight", "From", "To", "Departure", "Price", "Seats");
            while (rs.next()) {
                System.out.printf("%-4d %-10s %-15s %-15s %-20s %-10.2f %-6d%n",
                    rs.getInt("flight_id"), rs.getString("flight_no"), rs.getString("source"),
                    rs.getString("destination"), rs.getTimestamp("departure_time"),
                    rs.getDouble("price"), rs.getInt("available_seats"));
            }
        }
    }

    private static void searchFlights(Connection con) throws SQLException {
        String source = readText("Source city: ");
        String destination = readText("Destination city: ");
        String sql = "SELECT flight_id, flight_no, departure_time, price, available_seats FROM flights " +
                     "WHERE UPPER(source)=UPPER(?) AND UPPER(destination)=UPPER(?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, source); ps.setString(2, destination);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("ID: %d | %s | %s | Rs. %.2f | Seats: %d%n", rs.getInt(1), rs.getString(2), rs.getTimestamp(3), rs.getDouble(4), rs.getInt(5));
                }
                if (!found) System.out.println("No flights found.");
            }
        }
    }

    private static void bookFlight(Connection con) throws SQLException {
        int flightId = readInt("Flight ID: ");
        String name = readText("Passenger name: ");
        int age = readInt("Passenger age: ");
        String gender = readText("Gender (M/F/O): ");
        try (CallableStatement cs = con.prepareCall("{call book_flight(?, ?, ?, ?, ?)}")) {
            cs.setInt(1, flightId); cs.setString(2, name); cs.setInt(3, age); cs.setString(4, gender); cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            System.out.println("Booking successful. Booking ID: " + cs.getInt(5));
        } catch (SQLException e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    private static void viewBookings(Connection con) throws SQLException {
        String sql = "SELECT b.booking_id, p.passenger_name, f.flight_no, f.source, f.destination, b.booking_date, b.status " +
                     "FROM bookings b JOIN passengers p ON b.passenger_id=p.passenger_id JOIN flights f ON b.flight_id=f.flight_id ORDER BY b.booking_id";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) System.out.printf("Booking %d | %s | %s (%s-%s) | %s | %s%n", rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDate(6), rs.getString(7));
        }
    }

    private static void cancelBooking(Connection con) throws SQLException {
        int bookingId = readInt("Booking ID to cancel: ");
        try (CallableStatement cs = con.prepareCall("{call cancel_booking(?)}")) {
            cs.setInt(1, bookingId); cs.execute();
            System.out.println("Booking cancelled successfully.");
        } catch (SQLException e) { System.out.println("Cancellation failed: " + e.getMessage()); }
    }

    private static int readInt(String prompt) { System.out.print(prompt); return Integer.parseInt(input.nextLine().trim()); }
    private static String readText(String prompt) { System.out.print(prompt); return input.nextLine().trim(); }
}
