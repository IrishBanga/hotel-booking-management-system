package com.ib.hotelBookingManagementApplication.controllers;

import com.ib.hotelBookingManagementApplication.database.DatabaseUtil;
import com.ib.hotelBookingManagementApplication.model.*;
import com.ib.hotelBookingManagementApplication.utilities.Helper;
import com.ib.hotelBookingManagementApplication.utilities.ReservationHelper;
import com.ib.hotelBookingManagementApplication.utilities.RoomType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.ib.hotelBookingManagementApplication.utilities.Helper.formatCurrency;
import static com.ib.hotelBookingManagementApplication.utilities.Helper.showAlert;
import static com.ib.hotelBookingManagementApplication.utilities.LoggerUtil.logger;
import static com.ib.hotelBookingManagementApplication.utilities.ReservationHelper.*;

public class AdminController {

    static Map<RoomType, Pair<Integer, Double>> roomTypeData = DatabaseUtil.getInfoByRoomType();
    private final ObservableList<Guest> guestList = FXCollections.observableArrayList();
    private final ObservableList<Guest> filteredGuestList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    List<Room> reservationRoomList = new ArrayList<>();
    Map<RoomType, List<Room>> roomTypeMap = new HashMap<>();
    Admin admin = null;
    Billing billing;

    private Room room = null;
    @FXML
    private TextField applicableTax;
    @FXML
    private Button bookRoomButton;
    @FXML
    private Tab bookingDetailsTab;
    @FXML
    private ListView<Reservation> bookingsListView;
    @FXML
    private Button cancelBookingButton;
    @FXML
    private Button cancelButtonLoginTab;
    @FXML
    private Tab checkoutTab;
    @FXML
    private Button closeButton;
    @FXML
    private Button closeButtonRoomsTab;
    @FXML
    private Button confirmCheckoutButton;
    @FXML
    private TextArea costBreakdown;
    @FXML
    private TextField datesOfStay;
    @FXML
    private TextField deluxeRoomsCount;
    @FXML
    private Slider discountSlider;
    @FXML
    private TextField doubleRoomCount;
    @FXML
    private DatePicker endDate;
    @FXML
    private TextField estimatedTotal;
    @FXML
    private Button goBackBookingDetailsButton;
    @FXML
    private Button goBackCheckoutButton;
    @FXML
    private TextField guestEmail;
    @FXML
    private TextField guestEmailCheckoutPage;
    @FXML
    private TextField guestName;
    @FXML
    private TextField guestNameCheckoutPage;
    @FXML
    private Tab guestOverviewTab;
    @FXML
    private TextField guestPhone;
    @FXML
    private TextField guestPhoneCheckoutPage;
    @FXML
    private ListView<Guest> guestsListView;
    @FXML
    private Button loginButton;
    @FXML
    private Tab loginTab;
    @FXML
    private TextField numberOfGuests;
    @FXML
    private TextField numberOfGuestsCheckoutPage;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField pentHousesCount;
    @FXML
    private Button proceedToCheckoutButton;
    @FXML
    private Button newBookingButton;
    @FXML
    private ListView<Room> roomsListView;
    @FXML
    private Tab roomsTab;
    @FXML
    private Button saveBookingButton;
    @FXML
    private TextField searchGuestsTextField;
    @FXML
    private Button seeBookingDetailsButton;
    @FXML
    private TextField singleRoomCount;
    @FXML
    private DatePicker startDate;
    @FXML
    private TabPane tabPane;
    @FXML
    private TextField totalCost;
    @FXML
    private TextField usernameField;

    private ObservableList<Room> roomList = FXCollections.observableArrayList();
    private Reservation reservation = new Reservation();
    private Guest guest = new Guest(-1, "", "", "");

    @FXML
    public void initialize() {

        guestName.setOnKeyReleased(Helper::stringInputValidation);
        guestPhone.setOnKeyReleased(Helper::phoneInputValidation);
        numberOfGuests.setOnKeyReleased(Helper::numericIntegerInputValidation);
        singleRoomCount.setOnKeyReleased(Helper::numericIntegerInputValidation);
        doubleRoomCount.setOnKeyReleased(Helper::numericIntegerInputValidation);
        deluxeRoomsCount.setOnKeyReleased(Helper::numericIntegerInputValidation);
        pentHousesCount.setOnKeyReleased(Helper::numericIntegerInputValidation);

        searchGuestsTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                guestsListView.setItems(guestList);
            } else {
                filteredGuestList.clear();
                for (Guest guest : guestList) {
                    if (guest.getName().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredGuestList.add(guest);
                    }
                }
                logger.log(Level.INFO, admin.getUsername() + " searched for guest: " + newValue);
                guestsListView.setItems(filteredGuestList);
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == roomsTab) {
                logger.log(Level.INFO, admin.getUsername() + " navigated to rooms tab");
                roomList = FXCollections.observableArrayList(DatabaseUtil.getAllRooms());
                roomsListView.setItems(roomList);
            } else if (newValue == bookingDetailsTab && room == null) {
                switchFieldState(true);
            } else if (newValue == guestOverviewTab) {
                getAllData();
            }
        });

        guestsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Guest selectedGuest = guestsListView.getSelectionModel().getSelectedItem();
                if (selectedGuest != null) {
                    logger.log(Level.INFO, admin.getUsername() + " viewed guest details: " + selectedGuest.getName());
                    reservationList.clear();
                    reservationList.addAll(DatabaseUtil.getAllReservationsByGuest(selectedGuest.getGuestID()));
                    bookingsListView.setItems(reservationList);
                }
            }
        });

        discountSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double discount = newValue.doubleValue();
            double total = reservationRoomList.stream().mapToDouble(Room::getPrice).sum();
            double discountedTotal = total - (total * (discount / 100));
            double updatedTax = discountedTotal * 0.13;
            double finalTotal = discountedTotal + updatedTax;
            applicableTax.setText(formatCurrency(updatedTax));
            totalCost.setText(formatCurrency(finalTotal));
        });

        seeBookingDetailsButton.setOnAction(event -> {
            guest = guestsListView.getSelectionModel().getSelectedItem();
            reservation = bookingsListView.getSelectionModel().getSelectedItem();
            if (reservation != null) {
                logger.log(Level.INFO, admin.getUsername() + " viewed booking details for reservation ID: " +
                        reservation.getReservationID() + " and guest ID: " + guest.getGuestID());
                tabPane.getSelectionModel().select(bookingDetailsTab);
                populateBookingDetails(reservation, guest, roomList);
            }
        });

        proceedToCheckoutButton.setOnAction(event -> {
            populateBookingDetailsCheckOutPage();
        });

        confirmCheckoutButton.setOnAction(event -> {
            checkoutAndGenerateBill();
        });

        cancelBookingButton.setOnAction(event -> {

            reservation = bookingsListView.getSelectionModel().getSelectedItem();
            if (reservation != null) {
                if (!DatabaseUtil.cancelReservation(reservation.getReservationID())) {
                    showAlert(Alert.AlertType.ERROR, "Cancellation Failed",
                            """
                                    The reservation couldn't be cancelled. Please try again.
                                    """);
                }
                reservationList.remove(reservation);
                bookingsListView.setItems(reservationList);

                tabPane.getSelectionModel().select(guestOverviewTab);

                logger.log(Level.INFO, "Admin cancelled reservation with ID: " + reservation.getReservationID());

                showAlert(Alert.AlertType.INFORMATION, "Cancellation Successful!",
                        """
                                The reservation has been cancelled successfully.
                                """);
                resetPage();
            }
        });


        goBackCheckoutButton.setOnAction(event -> {
            tabPane.getSelectionModel().select(bookingDetailsTab);
        });

        goBackBookingDetailsButton.setOnAction(event -> {
            tabPane.getSelectionModel().select(guestOverviewTab);
        });

        closeButtonRoomsTab.setOnAction(event -> {
            tabPane.getSelectionModel().select(guestOverviewTab);
        });


        newBookingButton.setOnAction(event -> {
            logger.log(Level.INFO, admin.getUsername() + " started a new booking");
            tabPane.getSelectionModel().select(bookingDetailsTab);
            resetPage();
        });

        roomsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                bookRoomButton.fire();
            }
        });

        bookRoomButton.setOnAction(event -> {
            resetPage();
            room = roomsListView.getSelectionModel().getSelectedItem();

            if (room == null) {
                showAlert(Alert.AlertType.ERROR, "No Room Selected",
                        "Please select a room to book.");
                return;
            }

            reservation.setRoomsList(new ArrayList<>(room.getRoomID()));

            estimatedTotal.setText(formatCurrency(room.getPrice() * 1.13));

            switchFieldState(false);
            singleRoomCount.setText("0");
            doubleRoomCount.setText("0");
            pentHousesCount.setText("0");
            deluxeRoomsCount.setText("0");

            switch (room.getRoomType()) {
                case SINGLE -> singleRoomCount.setText("1");
                case DOUBLE -> doubleRoomCount.setText("1");
                case PENTHOUSE -> pentHousesCount.setText("1");
                case DELUXE -> deluxeRoomsCount.setText("1");
            }

            logger.log(Level.INFO, admin.getUsername() + " initiated booking for room: " + room.getRoomID());
            tabPane.getSelectionModel().select(bookingDetailsTab);
        });

        saveBookingButton.setOnAction(event -> {
            if (room != null) {
                customRoomBooking(room);
            } else {
                saveOrUpdateBooking();
            }
        });

    }

    private void customRoomBooking(Room room) {
        if (!validateBasicInput(startDate, endDate, numberOfGuests)) return;
        if (!validateDates(startDate, endDate)) return;
        if (!validateGuestInfo(guestName, guestEmail, guestPhone)) return;
        int guests = Integer.parseInt(numberOfGuests.getText());

        reservation.setCheckinDate(startDate.getValue());
        reservation.setCheckoutDate(endDate.getValue());
        reservation.setNumberOfGuests(guests);

        List<Integer> roomList = DatabaseUtil.getRoomsOccupiedBetweenDates(reservation.getCheckinDate(),
                reservation.getCheckoutDate());

        if (roomList.contains(room.getRoomID())) {
            showAlert(Alert.AlertType.ERROR, "Room Unavailable",
                    "The selected room is not available for the selected dates.");
            return;
        }

        if (guests <= 0 || guests > room.getNumberOfBeds()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                    "Number of guests must be a positive number and less than or equal to the number of beds in the room.");
            return;
        }

        reservation.setRoomsList(List.of(room.getRoomID()));

        boolean success = DatabaseUtil.createCompleteReservation(new Guest(guestName.getText().trim(),
                guestPhone.getText().trim(), guestEmail.getText().trim()), reservation, List.of(room.getRoomID()));

        if (!success) {
            showAlert(Alert.AlertType.ERROR, "An error occurred",
                    "Reservation couldn't be created. Please try again.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Room Booking", String.format("""
                        Room %d booked successfully!
                        Reservation ID: %d
                        Guest ID: %d
                        """,
                room.getRoomID(), reservation.getReservationID(), reservation.getGuestID()));

        logger.log(Level.INFO, admin.getUsername() + " booked room with ID: " + room.getRoomID() +
                " for guest ID: " + reservation.getGuestID());
        resetPage();
        tabPane.getSelectionModel().select(guestOverviewTab);
    }

    private void switchFieldState(boolean state) {
        singleRoomCount.setEditable(state);
        doubleRoomCount.setEditable(state);
        pentHousesCount.setEditable(state);
        deluxeRoomsCount.setEditable(state);
    }

    private void resetPage() {
        guestName.setText("");
        guestPhone.setText("");
        guestEmail.setText("");
        numberOfGuests.setText("");
        startDate.setValue(null);
        endDate.setValue(null);
        singleRoomCount.setText("");
        doubleRoomCount.setText("");
        pentHousesCount.setText("");
        deluxeRoomsCount.setText("");
        numberOfGuests.setText("");
        guestNameCheckoutPage.setText("");
        guestPhoneCheckoutPage.setText("");
        guestEmailCheckoutPage.setText("");
        numberOfGuestsCheckoutPage.setText("");
        datesOfStay.setText("");
        costBreakdown.setText("");
        estimatedTotal.setText("");
        applicableTax.setText("");
        totalCost.setText("");
        discountSlider.setValue(0);

        //Reset reservation and guest
        reservation = new Reservation();
        guest = new Guest(-1, "", "", "");
        room = null;
        switchFieldState(true);
    }

    private void saveOrUpdateBooking() {
        if (!validateBasicInput(startDate, endDate, numberOfGuests)) return;
        if (!validateDates(startDate, endDate)) return;
        if (!validateGuestInfo(guestName, guestEmail, guestPhone)) return;

        boolean newBooking = reservation.getReservationID() == -1;
        int guests = Integer.parseInt(numberOfGuests.getText());
        if (guests <= 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Number of guests must be a positive number.");
            return;
        }

        reservation.setCheckinDate(startDate.getValue());
        reservation.setCheckoutDate(endDate.getValue());
        reservation.setNumberOfGuests(guests);

        List<Integer> roomCounts = validateRoomCounts(singleRoomCount, doubleRoomCount, deluxeRoomsCount, pentHousesCount);

        if (roomCounts == null) {
            return;
        }

        int singleRooms = roomCounts.get(0);
        int doubleRooms = roomCounts.get(1);
        int deluxeSuites = roomCounts.get(2);
        int pentHouses = roomCounts.get(3);

        List<Room> roomList = DatabaseUtil.getAllRooms();
        List<Integer> occupiedRooms = DatabaseUtil.getRoomsOccupiedBetweenDates(reservation.getCheckinDate(),
                reservation.getCheckoutDate());

        List<Integer> roomsInReservation = reservation.getRoomsList();
        List<Room> availableRooms = roomList.stream().filter(room ->
                !occupiedRooms.contains(room.getRoomID()) || roomsInReservation.contains(room.getRoomID())).toList();

        Map<RoomType, List<Room>> roomsByType = ReservationHelper.loadRoomsByType(availableRooms);
        if (!ReservationHelper.validateRoomSelection(reservation, roomsByType, singleRooms, doubleRooms,
                deluxeSuites, pentHouses))
            return;

        Set<Room> roomListForReservation = loadRoomListForReservation(availableRooms, singleRooms, doubleRooms,
                deluxeSuites, pentHouses);

        reservation.setRoomsList(roomListForReservation.stream().map(Room::getRoomID).collect(Collectors.toList()));

        Pair<Double, String> totalCosts = ReservationHelper.getTotalCosts(singleRooms, doubleRooms, deluxeSuites, pentHouses);

        estimatedTotal.setText(formatCurrency(totalCosts.getKey() * 1.13));

        String name = guestName.getText().trim();
        String email = guestEmail.getText().trim();
        String phone = guestPhone.getText().trim();

        boolean success = DatabaseUtil.createCompleteReservation(new Guest(name, phone, email), reservation,
                roomListForReservation.stream().map(Room::getRoomID).collect(Collectors.toList()));

        if (!success) {
            showAlert(Alert.AlertType.ERROR, "An error occurred",
                    "Reservation couldn't be created. Please try again.");
            return;
        }

        String confirmationMessage = """
                Booking confirmed!
                Reservation ID: %d
                Guest ID: %d
                """;
        String updateMessage = """
                Booking updated successfully!
                Reservation ID: %d
                Guest ID: %d
                """;

        if (newBooking) {
            showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed", String.format(confirmationMessage,
                    reservation.getReservationID(), reservation.getGuestID()));
            logger.log(Level.INFO, admin.getUsername() + " made a new booking with ID: " +
                    reservation.getReservationID() + " for guest ID: " + reservation.getGuestID());
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Booking Updated", String.format(updateMessage,
                    reservation.getReservationID(), reservation.getGuestID()));
            logger.log(Level.INFO, admin.getUsername() + " updated booking with ID: " +
                    reservation.getReservationID() + " for guest ID: " + reservation.getGuestID());
        }

        resetPage();
        tabPane.getSelectionModel().select(guestOverviewTab);
    }

    private void populateBookingDetails(Reservation selectedReservation, Guest selectedGuest, List<Room> allRoomList) {
        guestName.setText(selectedGuest.getName());
        guestPhone.setText(selectedGuest.getPhoneNumber());
        guestEmail.setText(selectedGuest.getEmail());
        numberOfGuests.setText(String.valueOf(selectedReservation.getNumberOfGuests()));
        startDate.setValue(LocalDate.parse(selectedReservation.getCheckinDate().toString()));
        endDate.setValue(LocalDate.parse(selectedReservation.getCheckoutDate().toString()));

        reservationRoomList = new ArrayList<>();


        if (selectedReservation.getRoomsList() != null) {
            for (Integer roomID : selectedReservation.getRoomsList()) {
                for (Room room : allRoomList) {
                    if (room.getRoomID() == roomID) {
                        reservationRoomList.add(room);
                    }
                }
            }
        }

        roomTypeMap = new HashMap<>();
        for (Room room : reservationRoomList) {
            if (!roomTypeMap.containsKey(room.getRoomType())) {
                roomTypeMap.put(room.getRoomType(), new ArrayList<>());
            }
            roomTypeMap.get(room.getRoomType()).add(room);
        }

        singleRoomCount.setText(String.valueOf(roomTypeMap.getOrDefault(RoomType.SINGLE, new ArrayList<>()).size()));
        doubleRoomCount.setText(String.valueOf(roomTypeMap.getOrDefault(RoomType.DOUBLE, new ArrayList<>()).size()));
        deluxeRoomsCount.setText(String.valueOf(roomTypeMap.getOrDefault(RoomType.DELUXE, new ArrayList<>()).size()));
        pentHousesCount.setText(String.valueOf(roomTypeMap.getOrDefault(RoomType.PENTHOUSE, new ArrayList<>()).size()));

        estimatedTotal.setText(formatCurrency(reservationRoomList.stream().mapToDouble(Room::getPrice).sum() * 1.13));

        logger.log(Level.INFO, "Admin viewed booking details for reservation ID: " +
                selectedReservation.getReservationID() + " and guest ID: " + selectedGuest.getGuestID());
    }

    private void populateBookingDetailsCheckOutPage() {
        Guest selectedGuest = guestsListView.getSelectionModel().getSelectedItem();
        Reservation selectedReservation = bookingsListView.getSelectionModel().getSelectedItem();

        if (selectedReservation != null) {
            tabPane.getSelectionModel().select(checkoutTab);
            guestNameCheckoutPage.setText(selectedGuest.getName());
            guestPhoneCheckoutPage.setText(selectedGuest.getPhoneNumber());
            guestEmailCheckoutPage.setText(selectedGuest.getEmail());
            numberOfGuestsCheckoutPage.setText(String.valueOf(selectedReservation.getNumberOfGuests()));
            datesOfStay.setText((selectedReservation.getCheckinDate().toString() + " to " + selectedReservation.getCheckoutDate().toString()));


            int srCount = Integer.parseInt(singleRoomCount.getText());
            int drCount = Integer.parseInt(doubleRoomCount.getText());
            int drlCount = Integer.parseInt(deluxeRoomsCount.getText());
            int phCount = Integer.parseInt(pentHousesCount.getText());

            costBreakdown.setText(
                    "Single Rooms: " + srCount + " Price: " + (srCount * roomTypeData.get(RoomType.SINGLE).getValue()) + "\n" +
                            "Double Rooms: " + drCount + " Price: " + (drCount * roomTypeData.get(RoomType.DOUBLE).getValue()) + "\n" +
                            "Deluxe Rooms: " + drlCount + " Price: " + (drlCount * roomTypeData.get(RoomType.DELUXE).getValue()) + "\n" +
                            "Penthouse:    " + phCount + " Price: " + (phCount * roomTypeData.get(RoomType.PENTHOUSE).getValue()) + "\n");

            double totalRoomCost = reservationRoomList.stream().mapToDouble(Room::getPrice).sum();
            double tax = totalRoomCost * 0.13;
            double finalTotal = totalRoomCost + tax;
            applicableTax.setText(formatCurrency(tax));

            totalCost.setText(formatCurrency(finalTotal));
            discountSlider.setValue(0);

            billing = new Billing(selectedReservation.getReservationID(), totalRoomCost, discountSlider.getValue());

            logger.log(Level.INFO, admin.getUsername() + " viewed checkout details for reservation ID: " +
                    selectedReservation.getReservationID() + " and guest ID: " + selectedGuest.getGuestID());
        }
    }

    private void checkoutAndGenerateBill() {
        billing.setDiscount(discountSlider.getValue());

        boolean success = DatabaseUtil.completeCheckout(billing);
        if (!success) {
            showAlert(Alert.AlertType.ERROR, "An error occurred",
                    "Checkout couldn't be completed. Please try again.");
            return;
        }

        reservationList.remove(reservation);
        bookingsListView.setItems(reservationList);

        String successMessage = """
                Checkout Successful!
                Reservation ID: %d
                Guest ID: %d
                Room Cost: %.2f
                Discount Applied: %.2f%%
                Applicable Tax: %.2f
                Total Cost: %.2f
                """;

        showAlert(Alert.AlertType.INFORMATION, "Checkout Successful!", String.format(successMessage,
                reservation.getReservationID(), reservation.getGuestID(), billing.getAmount(), billing.getDiscount(),
                billing.getTax(), billing.getTotalAmount()));
        logger.log(Level.INFO, admin.getUsername() + " checked out reservation ID: " +
                reservation.getReservationID() + " for guest ID: " + reservation.getGuestID());

        resetPage();
        tabPane.getSelectionModel().select(guestOverviewTab);
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        if (!usernameField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            admin = new Admin(usernameField.getText(), passwordField.getText());
            // validate the login credentials
            if (admin.login()) {
                loginTab.setDisable(true);
                tabPane.getSelectionModel().select(guestOverviewTab);
                guestOverviewTab.setDisable(false);
                bookingDetailsTab.setDisable(false);
                checkoutTab.setDisable(false);
                roomsTab.setDisable(false);

                logger.log(Level.INFO, admin.getUsername() + " logged in successfully.");

                getAllData();

                System.out.println("Login Successful");
            } else {
                System.out.println("Login Failed");
                showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid username or password");
                usernameField.setText("");
                passwordField.setText("");
                usernameField.requestFocus();
            }
        } else if (usernameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Username is required");
        } else if (passwordField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Password is required");
        }
    }

    private void getAllData() {
        // Fetch all data from the database
        guestList.clear();
        guestList.addAll(DatabaseUtil.getAllGuests());
        roomList.clear();
        roomList.addAll(DatabaseUtil.getAllRooms());
        reservationList.clear();

        guestsListView.setItems(guestList);
    }

    public void init(Consumer<Boolean> onCancellation) {
        cancelButtonLoginTab.setOnAction(event -> onCancellation.accept(true));
        closeButton.setOnAction(event -> onCancellation.accept(true));
    }
}
