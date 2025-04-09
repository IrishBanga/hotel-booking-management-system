package com.ib.hotelBookingManagementApplication.database;

import com.ib.hotelBookingManagementApplication.model.*;
import com.ib.hotelBookingManagementApplication.utilities.RoomType;
import javafx.util.Pair;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.ib.hotelBookingManagementApplication.utilities.Helper.getProjectPath;
import static com.ib.hotelBookingManagementApplication.utilities.LoggerUtil.logger;

public class DatabaseUtil {

    private static final String DB_CONNECTION = "jdbc:sqlite:" + getProjectPath() +
            "\\src\\main\\java\\com\\ib\\hotelBookingManagementApplication\\database\\hotel.db";

    static {
        initializeDatabase();
    }

    private static void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION);
             Statement statement = connection.createStatement()) {

            // Create Admins table
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Admins (
                    AdminID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Username TEXT UNIQUE NOT NULL,
                    Password TEXT NOT NULL)
                    """);

            statement.executeUpdate(
                    """
                            INSERT OR IGNORE INTO ADMINS (Username, Password)
                            VALUES ('ib', 'ib'), ('admin', 'admin')
                            """
            );
            // Create Guests table
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Guests (
                    GuestID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Name TEXT NOT NULL,
                    PhoneNumber TEXT NOT NULL UNIQUE,
                    Email TEXT NOT NULL UNIQUE)
                    """);

            // Create RoomTypes table
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS RoomTypes (
                    RoomType TEXT PRIMARY KEY CHECK(RoomType IN ('SINGLE', 'DOUBLE', 'DELUXE', 'PENTHOUSE')),
                    NumberOfBeds INTEGER NOT NULL,
                    Price REAL NOT NULL)
                    """);

            // Insert default room types
            String[] roomTypes = {"SINGLE", "DOUBLE", "DELUXE", "PENTHOUSE"};
            int[] roomCounts = {35, 50, 10, 5}; // Counts for SINGLE, DOUBLE, DELUXE, PENTHOUSE

            for (String roomType : roomTypes) {
                String insertRoomType = "INSERT OR IGNORE INTO RoomTypes (RoomType, NumberOfBeds, Price) VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertRoomType)) {
                    preparedStatement.setString(1, roomType);
                    preparedStatement.setInt(2, roomType.equals("DOUBLE") ? 4 : 2);
                    preparedStatement.setDouble(3, roomType.equals("SINGLE") ? 450.0 :
                            roomType.equals("DOUBLE") ? 700.0 : roomType.equals("DELUXE") ? 1200.0 : 1500.0);
                    preparedStatement.executeUpdate();
                }
            }

            // Create Rooms table
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Rooms (
                    RoomID INTEGER PRIMARY KEY AUTOINCREMENT,
                    RoomType TEXT NOT NULL CHECK(RoomType IN ('SINGLE', 'DOUBLE', 'DELUXE', 'PENTHOUSE')),
                    FOREIGN KEY (RoomType) REFERENCES RoomTypes(RoomType))
                    """);

            for (int i = 0; i < roomTypes.length; i++) {
                for (int j = 0; j < roomCounts[i]; j++) {
                    String query = """
                            SELECT COUNT(*) FROM Rooms WHERE RoomType = ?
                            """;
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, roomTypes[i]);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (resultSet.next() && resultSet.getInt(1) < roomCounts[i]) {
                            String insertRoom = "INSERT INTO Rooms (RoomType) VALUES (?)";
                            try (PreparedStatement insertPreparedStatement = connection.prepareStatement(insertRoom)) {
                                insertPreparedStatement.setString(1, roomTypes[i]);
                                insertPreparedStatement.executeUpdate();
                            }
                        }
                    }
                }
            }

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Reservations (
                    ReservationID INTEGER PRIMARY KEY AUTOINCREMENT,
                    GuestID INTEGER NOT NULL,
                    CheckInDate DATE NOT NULL,
                    CheckOutDate DATE NOT NULL,
                    NumberOfGuests INTEGER NOT NULL,
                    Status TEXT NOT NULL CHECK(Status IN ('CONFIRMED', 'CANCELLED', 'CHECKED_OUT')),
                    FOREIGN KEY (GuestID) REFERENCES Guests(GuestID))
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Reservations_Rooms (
                    ReservationID INTEGER NOT NULL,
                    RoomID INTEGER NOT NULL,
                    PRIMARY KEY (ReservationID, RoomID),
                    FOREIGN KEY (ReservationID) REFERENCES Reservations(ReservationID),
                    FOREIGN KEY (RoomID) REFERENCES Rooms(RoomID))
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Billing (
                    BillID INTEGER PRIMARY KEY AUTOINCREMENT,
                    ReservationID INTEGER NOT NULL,
                    Amount REAL NOT NULL,
                    Discount REAL DEFAULT 0.0 CHECK(Discount >= 0 AND Discount <=25),
                    Tax REAL NOT NULL,
                    TotalAmount REAL NOT NULL,
                    FOREIGN KEY (ReservationID) REFERENCES Reservations(ReservationID))
                    """);

            statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS Feedback (
                        GuestID INTEGER NOT NULL,
                        ReservationID INTEGER NOT NULL,
                        Comments TEXT NOT NULL,
                        Rating REAL CHECK(Rating >= 0 AND Rating <= 5),
                        PRIMARY KEY (GuestID, ReservationID),
                        FOREIGN KEY (GuestID) REFERENCES Guests(GuestID),
                        FOREIGN KEY (ReservationID) REFERENCES Reservations(ReservationID))
                    """);

            System.out.println("Database connection established and initialized at: " + DB_CONNECTION);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database initialization failed: " + e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static int validateLogin(String name, String password) {
        String query = "SELECT * FROM Admins WHERE Username = ? AND Password = ?";
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("AdminID");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to validate login: " + e.getMessage(), e);
        }
        return 0;
    }

    public static boolean completeCheckout(Billing billing) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_CONNECTION);
            connection.setAutoCommit(false);

            String billingStatement = """
                        INSERT INTO Billing (ReservationID, Amount, Discount, Tax, TotalAmount)
                        VALUES (?, ?, ?, ?, ?);
                    """;

            try (PreparedStatement billingStmt = connection.prepareStatement(billingStatement)) {
                billingStmt.setInt(1, billing.getReservationID());
                billingStmt.setDouble(2, billing.getAmount());
                billingStmt.setDouble(3, billing.getDiscount());
                billingStmt.setDouble(4, billing.getTax());
                billingStmt.setDouble(5, billing.getTotalAmount());

                billingStmt.executeUpdate();
            }

            String checkoutStatement = """
                        UPDATE Reservations
                        SET Status = 'CHECKED_OUT'
                        WHERE ReservationID = ?;
                    """;

            try (PreparedStatement checkoutStmt = connection.prepareStatement(checkoutStatement)) {
                checkoutStmt.setInt(1, billing.getReservationID());
                checkoutStmt.executeUpdate();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to complete checkout: " + e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    logger.log(Level.SEVERE, "Failed to rollback checkout transaction", rollbackEx);
                }
            }
            return false;

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    logger.log(Level.SEVERE, "Failed to close connection after checkout", closeEx);
                }
            }
        }
    }

    public static Map<RoomType, Pair<Integer, Double>> getInfoByRoomType() {
        String query = "SELECT * FROM RoomTypes";
        Map<RoomType, Pair<Integer, Double>> roomInfo = new java.util.HashMap<>();

        try (Connection connection = DriverManager.getConnection(DB_CONNECTION); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String roomTypeStr = resultSet.getString("RoomType");
                int numberOfBeds = resultSet.getInt("NumberOfBeds");
                double roomPrice = resultSet.getDouble("Price");
                roomInfo.put(RoomType.valueOf(roomTypeStr), new Pair<>(numberOfBeds, roomPrice));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve room information by type", e);
        }
        return roomInfo;
    }

    public static boolean checkReservationInformation(int reservationID, int guestID) {
        String query = "SELECT * FROM Reservations WHERE ReservationID = ? AND GuestID = ? AND STATUS = 'CHECKED_OUT'";
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reservationID);
            preparedStatement.setInt(2, guestID);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check reservation information", e);
        }
    }

    public static boolean submitFeedback(Feedback feedback) {
        String statement = """
                    INSERT INTO Feedback (GuestID, ReservationID, Comments, Rating)
                    VALUES (?, ?, ?, ?);
                """;

        try (Connection connection = DriverManager.getConnection(DB_CONNECTION); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {

            preparedStatement.setInt(1, feedback.getGuestID());
            preparedStatement.setInt(2, feedback.getReservationID());
            preparedStatement.setString(3, feedback.getComments());
            preparedStatement.setDouble(4, feedback.getRating());

            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to submit feedback", e);
        }
        return false;
    }

    public static boolean checkFeedbackExists(int reservationID, int guestID) {
        String query = "SELECT * FROM Feedback WHERE ReservationID = ? AND GuestID = ?";
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reservationID);
            preparedStatement.setInt(2, guestID);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check feedback existence", e);
        }
    }

    public static List<Room> getAllRooms() {
        String query = "SELECT * FROM Rooms JOIN RoomTypes ON Rooms.RoomType = RoomTypes.RoomType";
        List<Room> rooms = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int roomId = resultSet.getInt("RoomID");
                String roomType = resultSet.getString("RoomType");
                int noOfBeds = resultSet.getInt("NumberOfBeds");
                float price = resultSet.getFloat("Price");
                rooms.add(new Room(roomId, RoomType.valueOf(roomType), noOfBeds, price));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve all rooms: " + e.getMessage(), e);
        }
        return rooms;
    }

    public static List<Guest> getAllGuests() {
        String query = "SELECT * FROM Guests";
        List<Guest> guests = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int guestId = resultSet.getInt("GuestID");
                String name = resultSet.getString("Name");
                String phoneNumber = resultSet.getString("PhoneNumber");
                String email = resultSet.getString("Email");
                guests.add(new Guest(guestId, name, phoneNumber, email));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve all guests", e);
        }
        return guests;
    }

    public static List<Reservation> getAllReservationsByGuest(int guestID) {
        String query = "SELECT * FROM Reservations WHERE GuestID = ? AND Status NOT IN ('CANCELLED', 'CHECKED_OUT')";
        String query2 = "SELECT * FROM Reservations_Rooms WHERE ReservationID = ?";
        List<Reservation> reservations = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, guestID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // Get the room IDs for the reservation
                List<Integer> roomIds = new ArrayList<>();
                try (PreparedStatement preparedStatement2 = connection.prepareStatement(query2)) {
                    preparedStatement2.setInt(1, resultSet.getInt("ReservationID"));
                    ResultSet resultSet2 = preparedStatement2.executeQuery();
                    while (resultSet2.next()) {
                        roomIds.add(resultSet2.getInt("RoomID"));
                    }
                }
                Reservation reservation = new Reservation(resultSet.getInt("ReservationID"),
                        resultSet.getInt("GuestID"), resultSet.getDate("CheckInDate").toLocalDate(),
                        resultSet.getDate("CheckOutDate").toLocalDate(), resultSet.getInt("NumberOfGuests"),
                        resultSet.getString("Status"));
                reservation.setRoomsList(roomIds);
                reservations.add(reservation);
            }
            if (reservations.isEmpty()) {
                logger.log(Level.INFO, "No reservations found for guest ID: " + guestID);
            } else {
                logger.log(Level.INFO, "Reservations found for guest ID: " + guestID);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve all reservations by guest ID: " + guestID, e);
        }
        return reservations;
    }

    public static List<Room> getAllRoomsByReservation(int reservationID) {
        String query = """
                SELECT Rooms.*, RoomTypes.NumberOfBeds, RoomTypes.Price
                FROM Rooms JOIN main.Reservations_Rooms RR on Rooms.RoomID = RR.RoomID
                JOIN RoomTypes ON Rooms.RoomType = RoomTypes.RoomType
                WHERE RR.ReservationID = ?""";

        List<Room> rooms = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reservationID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int roomId = resultSet.getInt("RoomID");
                String roomType = resultSet.getString("RoomType");
                int noOfBeds = resultSet.getInt("NumberOfBeds");
                float price = resultSet.getFloat("Price");
                rooms.add(new Room(roomId, RoomType.valueOf(roomType), noOfBeds, price));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve all reservations by room ID: " + reservationID, e);
        }
        return rooms;
    }

    public static List<Integer> getRoomsOccupiedBetweenDates(LocalDate checkinDate, LocalDate checkoutDate) {
        String query = """
                SELECT RoomID
                FROM Reservations JOIN Reservations_Rooms ON Reservations.ReservationID = Reservations_Rooms.ReservationID
                WHERE Reservations.CheckInDate <= ? AND Reservations.CheckOutDate >= ?
                AND Reservations.Status NOT IN  ('CANCELLED', 'CHECKED_OUT')
                """;

        List<Integer> occupiedRooms = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_CONNECTION);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(checkoutDate));
            preparedStatement.setDate(2, java.sql.Date.valueOf(checkinDate));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int roomId = resultSet.getInt("RoomID");
                occupiedRooms.add(roomId);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve occupied rooms between dates: " + e.getMessage(), e);
        }
        return occupiedRooms;
    }

    public static boolean cancelReservation(int reservationId) {
        String statement = """
                    UPDATE Reservations
                    SET Status = 'CANCELLED'
                    WHERE ReservationID = ?;
                """;

        try (Connection connection = DriverManager.getConnection(DB_CONNECTION);
             PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setInt(1, reservationId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to cancel reservation: " + e.getMessage(), e);
        }
        return false;
    }

    public static void setGuestDetails(Guest guest) {
        String statement = """
                UPDATE Guests
                SET Name = ?, Email = ?, PhoneNumber = ?
                WHERE GuestID = ?;
                """;

        try (Connection connection = DriverManager.getConnection(DB_CONNECTION);
             PreparedStatement preparedStatement = connection.prepareStatement(statement)) {

            preparedStatement.setString(1, guest.getName());
            preparedStatement.setString(2, guest.getEmail());
            preparedStatement.setString(3, guest.getPhoneNumber());
            preparedStatement.setInt(4, guest.getGuestID());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update guest details: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update guest details", e);
        }
    }

    private static void setGuestDetailsWithinTransaction(Connection connection, Guest guest) {
        String statement = """
                UPDATE Guests
                SET Name = ?, Email = ?, PhoneNumber = ?
                WHERE GuestID = ?;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, guest.getName());
            preparedStatement.setString(2, guest.getEmail());
            preparedStatement.setString(3, guest.getPhoneNumber());
            preparedStatement.setInt(4, guest.getGuestID());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update guest details within transaction: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update guest details within transaction", e);
        }
    }

    public static boolean createCompleteReservation(Guest guest, Reservation reservation, List<Integer> roomIds) {
        logger.log(Level.INFO, "DB initiating reservation transaction for reservation ID: " +
                reservation.getReservationID() + " and guest ID: " + guest.getGuestID());
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_CONNECTION);
            connection.setAutoCommit(false);

            int guestID = -1;
            if (guest.getGuestID() != -1) {
                setGuestDetailsWithinTransaction(connection, guest);
            } else {
                guestID = checkGuestWithinTransaction(connection, guest);
                if (guestID == -1) throw new SQLException("Guest creation failed");
            }
            reservation.setGuestID(guestID);
            if (reservation.getReservationID() != -1) {
                updateReservationWithinTransaction(connection, reservation);
                resetReservationRoomsWithinTransaction(connection, reservation.getReservationID());
                assignRoomsWithinTransaction(connection, reservation.getReservationID(), roomIds);
            } else {
                int reservationID = createReservationWithinTransaction(connection, reservation);
                if (reservationID == -1) throw new SQLException("Reservation creation failed");
                assignRoomsWithinTransaction(connection, reservationID, roomIds);
                reservation.setReservationID(reservationID);
            }

            connection.commit();
            return true;

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    logger.log(Level.SEVERE, "An error occurred while rolling back the transaction: " + e.getMessage()
                            , rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "An error occurred while creating the reservation: " + e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    logger.log(Level.SEVERE, "Failed to close connection: " + closeEx.getMessage(), closeEx);
                }
            }
        }
    }

    private static void resetReservationRoomsWithinTransaction(Connection connection, int reservationID) {
        logger.log(Level.INFO, "Resetting rooms for reservation ID: " + reservationID);

        String statement = """
                    DELETE FROM Reservations_Rooms
                    WHERE ReservationID = ?;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setInt(1, reservationID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to reset reservation rooms within transaction: " + e.getMessage(), e);
            throw new RuntimeException("Failed to reset reservation rooms within transaction", e);
        }
    }

    private static void updateReservationWithinTransaction(Connection connection, Reservation reservation) {
        logger.log(Level.INFO, "Updating reservation ID: " + reservation.getReservationID());

        String statement = """
                    UPDATE Reservations
                    SET CheckInDate = ?, CheckOutDate = ?, NumberOfGuests = ?
                    WHERE ReservationID = ?;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(reservation.getCheckinDate()));
            preparedStatement.setDate(2, java.sql.Date.valueOf(reservation.getCheckoutDate()));
            preparedStatement.setInt(3, reservation.getNumberOfGuests());
            preparedStatement.setInt(4, reservation.getReservationID());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update reservation within transaction: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update reservation within transaction", e);
        }
    }

    private static void assignRoomsWithinTransaction(Connection connection, int reservationID, List<Integer> roomIds) {
        logger.log(Level.INFO, "Assigning rooms to reservation ID: " + reservationID);

        String statement = """
                    INSERT INTO Reservations_Rooms (ReservationID, RoomID)
                    VALUES (?, ?);
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            for (int roomId : roomIds) {
                preparedStatement.setInt(1, reservationID);
                preparedStatement.setInt(2, roomId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to assign rooms within transaction: " + e.getMessage(), e);
            throw new RuntimeException("Failed to assign rooms within transaction", e);
        }
    }

    private static int createReservationWithinTransaction(Connection connection, Reservation reservation) {
        logger.log(Level.INFO, "Creating reservation for guest ID: " + reservation.getGuestID());

        String statement = """
                    INSERT INTO Reservations (GuestID, CheckInDate, CheckOutDate, NumberOfGuests, Status)
                    VALUES (?, ?, ?, ?, 'CONFIRMED');
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, reservation.getGuestID());
            preparedStatement.setDate(2, java.sql.Date.valueOf(reservation.getCheckinDate()));
            preparedStatement.setDate(3, java.sql.Date.valueOf(reservation.getCheckoutDate()));
            preparedStatement.setInt(4, reservation.getNumberOfGuests());

            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create reservation within transaction: " + e.getMessage(), e);

            throw new RuntimeException("Failed to create reservation within transaction", e);
        }
        return -1;
    }

    private static int checkGuestWithinTransaction(Connection connection, Guest guest) {
        logger.log(Level.INFO, "Checking if guest exists: " + guest.getName());

        String query = "SELECT * FROM Guests WHERE Name = ? AND PhoneNumber = ? AND Email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, guest.getName());
            preparedStatement.setString(2, guest.getPhoneNumber());
            preparedStatement.setString(3, guest.getEmail());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("GuestID");
            } else {
                return addGuestWithinTransaction(connection, guest);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to check guest within transaction: " + e.getMessage(), e);
            throw new RuntimeException("Failed to check guest within transaction", e);
        }
    }

    private static int addGuestWithinTransaction(Connection connection, Guest guest) {
        logger.log(Level.INFO, "Adding new guest: " + guest.getName());

        String statement = """
                    INSERT INTO Guests (Name, Email, PhoneNumber)
                    VALUES (?, ?, ?);
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, guest.getName());
            preparedStatement.setString(2, guest.getEmail());
            preparedStatement.setString(3, guest.getPhoneNumber());

            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to add guest within transaction: " + e.getMessage(), e);
            throw new RuntimeException("Failed to add guest within transaction", e);
        }
        return -1;
    }
}
