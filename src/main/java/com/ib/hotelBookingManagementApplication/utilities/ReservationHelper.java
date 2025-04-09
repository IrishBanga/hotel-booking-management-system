package com.ib.hotelBookingManagementApplication.utilities;

import com.ib.hotelBookingManagementApplication.database.DatabaseUtil;
import com.ib.hotelBookingManagementApplication.model.Reservation;
import com.ib.hotelBookingManagementApplication.model.Room;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static com.ib.hotelBookingManagementApplication.utilities.Helper.showAlert;

public class ReservationHelper {

    static Map<RoomType, Pair<Integer, Double>> roomTypeData = DatabaseUtil.getInfoByRoomType();

    public static boolean validateBasicInput(DatePicker startDate, DatePicker endDate, TextField numberOfGuests) {
        if (startDate.getValue() == null || endDate.getValue() == null || numberOfGuests.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Missing Input",
                    "Please fill all required fields: dates and number of guests.");
            return false;
        }
        return true;
    }

    public static boolean validateDates(DatePicker startDate, DatePicker endDate) {
        if (endDate.getValue().isBefore(startDate.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Invalid Dates", "End date must be after start date.");
            return false;
        }
        if (startDate.getValue().isBefore(java.time.LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Invalid Dates",
                    "Start date must be today or in the future.");
            return false;
        }
        return true;
    }

    public static boolean validateGuestInfo(TextField guestName, TextField guestEmail, TextField guestPhone) {
        String name = guestName.getText().trim();
        String email = guestEmail.getText().trim();
        String phone = guestPhone.getText().trim();
        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Missing Input",
                    "Please fill all required fields: guest information.");
            return false;
        }
        if (!email.matches("^(.+)@(\\S+)$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", """
                    Please enter a valid email address.
                    Example: example@company.ca
                    """);
            return false;
        }
        return true;
    }

    public static List<Integer> validateRoomCounts(TextField singleRoomCount, TextField doubleRoomCount,
                                                   TextField suiteRoomCount, TextField pentHouseCount) {
        if (singleRoomCount.getText().isBlank() && doubleRoomCount.getText().isBlank() && pentHouseCount.getText().isBlank()
                && suiteRoomCount.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Missing Input", "Please fill all required fields: room counts.");
            return null;
        }
        int singleRooms = singleRoomCount.getText().isBlank() ? 0 : Integer.parseInt(singleRoomCount.getText());
        int doubleRooms = doubleRoomCount.getText().isBlank() ? 0 : Integer.parseInt(doubleRoomCount.getText());
        int deluxeSuites = suiteRoomCount.getText().isBlank() ? 0 : Integer.parseInt(suiteRoomCount.getText());
        int pentHouses = pentHouseCount.getText().isBlank() ? 0 : Integer.parseInt(pentHouseCount.getText());

        return List.of(singleRooms, doubleRooms, deluxeSuites, pentHouses);
    }

    public static boolean validateRoomSelection(Reservation reservation, Map<RoomType, List<Room>> roomsByType,
                                                int singleRooms, int doubleRooms, int deluxeSuites, int pentHouses) {
        String errorMessage = """
                One or more of the room types selected exceeds the available rooms.
                Max rooms available:
                Single Rooms: %d | Double Rooms: %d
                Pent Houses: %d  | Deluxe Suites: %d
                """;

        if (singleRooms > roomsByType.getOrDefault(RoomType.SINGLE, new ArrayList<>()).size() ||
                doubleRooms > roomsByType.getOrDefault(RoomType.DOUBLE, new ArrayList<>()).size() ||
                pentHouses > roomsByType.getOrDefault(RoomType.PENTHOUSE, new ArrayList<>()).size() ||
                deluxeSuites > roomsByType.getOrDefault(RoomType.DELUXE, new ArrayList<>()).size()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", String.format(errorMessage,
                    roomsByType.getOrDefault(RoomType.SINGLE, new ArrayList<>()).size(),
                    roomsByType.getOrDefault(RoomType.DOUBLE, new ArrayList<>()).size(),
                    roomsByType.getOrDefault(RoomType.PENTHOUSE, new ArrayList<>()).size(),
                    roomsByType.getOrDefault(RoomType.DELUXE, new ArrayList<>()).size()));
            return false;
        }

        int sum = (singleRooms * roomTypeData.get(RoomType.SINGLE).getKey()) +
                (doubleRooms * roomTypeData.get(RoomType.DOUBLE).getKey()) +
                (deluxeSuites * roomTypeData.get(RoomType.DELUXE).getKey()) +
                (pentHouses * roomTypeData.get(RoomType.PENTHOUSE).getKey());

        if (sum < reservation.getNumberOfGuests()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                    "The number of rooms selected is not enough for the number of guests.");
            return false;
        }

        return true;
    }

    public static Pair<Double, String> getTotalCosts(int singleRooms, int doubleRooms, int deluxeSuites, int pentHouses) {
        Double totalCosts = 0.0;

        Double singleRoomPrice = roomTypeData.get(RoomType.SINGLE).getValue();
        Double doubleRoomPrice = roomTypeData.get(RoomType.DOUBLE).getValue();
        Double deluxeRoomPrice = roomTypeData.get(RoomType.DELUXE).getValue();
        Double pentHousePrice = roomTypeData.get(RoomType.PENTHOUSE).getValue();

        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Room Type\tPrice\n");
        if (singleRooms > 0) {
            breakdown.append("Single Room(s): ").append(singleRoomPrice * singleRooms).append("\n");
            totalCosts += singleRoomPrice * singleRooms;
        }
        if (doubleRooms > 0) {
            breakdown.append("Double Room(s): ").append(doubleRoomPrice * doubleRooms).append("\n");
            totalCosts += doubleRoomPrice * doubleRooms;
        }
        if (deluxeSuites > 0) {
            breakdown.append("Deluxe Suite(s): ").append(deluxeRoomPrice * deluxeSuites).append("\n");
            totalCosts += deluxeRoomPrice * deluxeSuites;
        }
        if (pentHouses > 0) {
            breakdown.append("Pent House(s): ").append(pentHousePrice * pentHouses).append("\n");
            totalCosts += pentHousePrice * pentHouses;
        }

        breakdown.append("Total: ").append(totalCosts).append("\n");

        return new Pair<>(totalCosts, breakdown.toString());
    }

    public static Set<Room> loadRoomListForReservation(List<Room> availableRooms, int singleRooms, int doubleRooms,
                                                       int deluxeSuites, int pentHouses) {

        Set<Room> roomListForReservation = new HashSet<>();

        for (Room room : availableRooms) {
            if (room.getRoomType() == RoomType.SINGLE && singleRooms > 0) {
                roomListForReservation.add(room);
                singleRooms--;
            } else if (room.getRoomType() == RoomType.DOUBLE && doubleRooms > 0) {
                roomListForReservation.add(room);
                doubleRooms--;
            } else if (room.getRoomType() == RoomType.PENTHOUSE && pentHouses > 0) {
                roomListForReservation.add(room);
                pentHouses--;
            } else if (room.getRoomType() == RoomType.DELUXE && deluxeSuites > 0) {
                roomListForReservation.add(room);
                deluxeSuites--;
            }
        }

        return roomListForReservation;
    }

    public static List<Room> loadRoomList() {
        return DatabaseUtil.getAllRooms();
    }

    public static List<Room> loadAvailableRooms(Reservation reservation, List<Room> roomList) {
        List<Integer> occupiedRooms = DatabaseUtil.getRoomsOccupiedBetweenDates(reservation.getCheckinDate(),
                reservation.getCheckoutDate());
        return roomList.stream().filter(room -> !occupiedRooms.contains(room.getRoomID())).toList();
    }

    public static Map<RoomType, List<Room>> loadRoomsByType(List<Room> availableRooms) {
        return availableRooms.stream().collect(Collectors.groupingBy(Room::getRoomType));
    }

}
