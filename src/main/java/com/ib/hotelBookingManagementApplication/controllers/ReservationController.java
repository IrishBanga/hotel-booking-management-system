package com.ib.hotelBookingManagementApplication.controllers;

import com.ib.hotelBookingManagementApplication.database.DatabaseUtil;
import com.ib.hotelBookingManagementApplication.model.Guest;
import com.ib.hotelBookingManagementApplication.model.Reservation;
import com.ib.hotelBookingManagementApplication.model.Room;
import com.ib.hotelBookingManagementApplication.utilities.Helper;
import com.ib.hotelBookingManagementApplication.utilities.ReservationHelper;
import com.ib.hotelBookingManagementApplication.utilities.RoomType;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.ib.hotelBookingManagementApplication.utilities.Helper.*;
import static com.ib.hotelBookingManagementApplication.utilities.LoggerUtil.logger;
import static com.ib.hotelBookingManagementApplication.utilities.ReservationHelper.*;

public class ReservationController {

    static Map<RoomType, Pair<Integer, Double>> roomTypeData = DatabaseUtil.getInfoByRoomType();
    private final Reservation reservation = new Reservation();
    private Map<RoomType, List<Room>> roomsByType = new HashMap<>();
    private Set<Room> roomListForReservation = new HashSet<>();
    private List<Room> roomList;
    private List<Room> availableRooms;
    @FXML
    private TextField applicableTax;
    @FXML
    private Button backButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextArea costBreakdown;
    @FXML
    private TextField datesOfStay;
    @FXML
    private TextField deluxeSuiteCount;
    @FXML
    private TextField doubleRoomCount;
    @FXML
    private DatePicker endDate;
    @FXML
    private TextField estimatedTotal;
    @FXML
    private TextField guestEmail;
    @FXML
    private TextField guestName;
    @FXML
    private TextField guestPhone;
    @FXML
    private Button informationAndHelpButton;
    @FXML
    private Button nextButton;
    @FXML
    private TextField numberGuests;
    @FXML
    private TextField numberOfGuestsConfirmationPage;
    @FXML
    private TextField pentHouseCount;
    @FXML
    private Label reservationStepperLabel;
    @FXML
    private TextField singleRoomCount;
    @FXML
    private DatePicker startDate;
    @FXML
    private Tab step1;
    @FXML
    private AnchorPane step1AP;
    @FXML
    private Tab step2;
    @FXML
    private AnchorPane step2AP;
    @FXML
    private Tab step3;
    @FXML
    private AnchorPane step3AP;
    @FXML
    private ListView<String> suggestionsListView;
    @FXML
    private TabPane tabPane;
    private Stage stage;
    private Scene scene;
    private int stepNo = 1;

    @FXML
    public void initialize() {

        numberGuests.setOnKeyReleased(Helper::numericIntegerInputValidation);

        singleRoomCount.setOnKeyReleased(Helper::numericIntegerInputValidation);
        doubleRoomCount.setOnKeyReleased(Helper::numericIntegerInputValidation);
        pentHouseCount.setOnKeyReleased(Helper::numericIntegerInputValidation);
        deluxeSuiteCount.setOnKeyReleased(Helper::numericIntegerInputValidation);

        guestName.setOnKeyReleased(Helper::stringInputValidation);
        guestPhone.setOnKeyReleased(Helper::phoneInputValidation);

        nextButton.setOnAction(event -> {
            if (stepNo == 1 && !validateReservationStep1()) return;
            if (stepNo == 2 && !validateReservationStep2()) return;
            if (stepNo < 3) switchToTab(++stepNo);
            else if (stepNo == 3) confirmBooking();
        });

        backButton.setOnAction(event -> {
            if (stepNo > 1) {
                switchToTab(--stepNo);
            }
        });

        informationAndHelpButton.setOnAction(event -> {
            setStageScene();
            HelpAndInformationController.loadHelpScreen(stage, scene);
        });
        reservationStepperLabel.setText("Please enter details of your stay...");

        suggestionsListView.setOnMouseClicked(this::handleSuggestionDoubleClick);
    }

    public void init(Consumer<Boolean> onCancellation) {
        cancelButton.setOnAction(event -> onCancellation.accept(true));
    }

    private boolean validateReservationStep1() {
        if (!ReservationHelper.validateBasicInput(startDate, endDate, numberGuests)) return false;
        if (!ReservationHelper.validateDates(startDate, endDate)) return false;

        int guests = Integer.parseInt(numberGuests.getText());
        if (guests <= 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                    "Number of guests must be a positive number.");
            return false;
        }
        reservation.setCheckinDate(startDate.getValue());
        reservation.setCheckoutDate(endDate.getValue());
        reservation.setNumberOfGuests(guests);

        return true;
    }

    private boolean validateReservationStep2() {
        List<Integer> roomCounts = validateRoomCounts(singleRoomCount, doubleRoomCount, deluxeSuiteCount, pentHouseCount);

        if (roomCounts == null) {
            return false;
        }

        int singleRooms = roomCounts.get(0);
        int doubleRooms = roomCounts.get(1);
        int deluxeSuites = roomCounts.get(2);
        int pentHouses = roomCounts.get(3);

        if (!validateRoomSelection(reservation, roomsByType, singleRooms, doubleRooms, deluxeSuites, pentHouses))
            return false;

        Pair<Double, String> costs = getTotalCosts(singleRooms, doubleRooms, deluxeSuites, pentHouses);
        double totalCosts = costs.getKey();
        String breakdown = costs.getValue();

        roomListForReservation = loadRoomListForReservation(availableRooms, singleRooms,
                doubleRooms, deluxeSuites, pentHouses);

        reservation.setRoomsList(roomListForReservation.stream().map(Room::getRoomID).collect(Collectors.toList()));

        double tax = totalCosts * 0.13;

        costBreakdown.setText(breakdown);
        applicableTax.setText(formatCurrency((tax)));
        estimatedTotal.setText(formatCurrency(totalCosts + tax));

        return true;
    }

    private void confirmBooking() {
        if (!ReservationHelper.validateGuestInfo(guestName, guestEmail, guestPhone)) return;

        String name = guestName.getText().trim();
        String email = guestEmail.getText().trim();
        String phone = guestPhone.getText().trim();

        AtomicBoolean confirm = new AtomicBoolean(true);

        showAlertWithCallback(
                Alert.AlertType.CONFIRMATION,
                "Confirm Booking",
                String.format("""
                                Please review your booking details:
                                Check-in Date: %s
                                Check-out Date: %s
                                Number of Guests: %s
                                Room(s) Selected: %s
                                Total Estimated Cost: %s
                                """,
                        reservation.getCheckinDate(),
                        reservation.getCheckoutDate(),
                        reservation.getNumberOfGuests(),
                        roomListForReservation.size(),
                        estimatedTotal.getText()),
                (response) -> {
                    if (response) {
                        boolean success = DatabaseUtil.createCompleteReservation(
                                new Guest(name, phone, email),
                                reservation,
                                roomListForReservation.stream().map(Room::getRoomID).collect(Collectors.toList())
                        );

                        if (!success) {
                            showAlert(Alert.AlertType.ERROR, "An error occurred",
                                    "Reservation couldn't be created. Please try again.");
                            return;
                        }

                        String confirmationMessage = """
                                Your booking has been confirmed. Thank you!
                                Reservation ID for your booking: %d
                                Your Guest ID: %d
                                PLEASE NOTE: To check-out and make payments after your stay, please contact the hotel directly.
                                """;

                        showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed", String.format(confirmationMessage,
                                reservation.getReservationID(), reservation.getGuestID()));

                    } else {
                        confirm.set(false);
                    }
                });

        if (!confirm.get()) {
            return;
        }

        logger.info("Booking confirmed for Guest ID: " + reservation.getGuestID() +
                ", Reservation ID: " + reservation.getReservationID());
        cancelButton.fire();
    }

    private void setStageScene() {
        stage = (Stage) informationAndHelpButton.getScene().getWindow();
        scene = informationAndHelpButton.getScene();
    }

    private void switchToTab(int stepNo) {
        switch (stepNo) {
            case 1 -> {
                tabPane.getSelectionModel().select(0);
                backButton.setDisable(true);
                nextButton.setText("Next");
                reservationStepperLabel.setText("Please enter details of your stay...");
            }
            case 2 -> {
                tabPane.getSelectionModel().select(1);
                backButton.setDisable(false);
                nextButton.setText("Next");
                reservationStepperLabel.setText("Please select your room(s)...");

                // Load room suggestions for Guests
                loadRoomData();
                generateRoomSuggestions(reservation.getNumberOfGuests());
            }
            case 3 -> {
                tabPane.getSelectionModel().select(2);

                backButton.setDisable(false);
                nextButton.setText("Confirm Booking");
                reservationStepperLabel.setText("Reservation Confirmation");
                loadFinalConfirmationPage();
            }
        }
    }

    private void loadRoomData() {
        roomList = ReservationHelper.loadRoomList();
        availableRooms = ReservationHelper.loadAvailableRooms(reservation, roomList);
        roomsByType = ReservationHelper.loadRoomsByType(availableRooms);
    }

    private void loadFinalConfirmationPage() {
        datesOfStay.setText(reservation.getCheckinDate() + " to " + reservation.getCheckoutDate());
        numberOfGuestsConfirmationPage.setText(String.valueOf(reservation.getNumberOfGuests()));
    }

    private void generateRoomSuggestions(int noOfGuests) {
        List<List<Pair<RoomType, Integer>>> suggestions = new ArrayList<>();
        List<RoomType> roomTypePriority = Arrays.asList(RoomType.PENTHOUSE, RoomType.DELUXE, RoomType.DOUBLE, RoomType.SINGLE);

        generateSuggestions(roomTypePriority, 0, noOfGuests == 1 ? 2 : noOfGuests, new ArrayList<>(), suggestions);

        suggestionsListView.getItems().clear();
        for (List<Pair<RoomType, Integer>> suggestion : suggestions) {
            StringBuilder sb = new StringBuilder();
            for (Pair<RoomType, Integer> rtc : suggestion) {
                if (rtc.getValue() > 0) {
                    sb.append(rtc.getKey()).append(" x").append(rtc.getValue()).append(", ");
                }
            }
            if (!sb.isEmpty()) {
                sb.setLength(sb.length() - 2);
            }
            suggestionsListView.getItems().add(sb.toString());
        }
    }

    private void generateSuggestions(List<RoomType> roomTypes, int index, int remainingGuests,
                                     List<Pair<RoomType, Integer>> currentSuggestion,
                                     List<List<Pair<RoomType, Integer>>> suggestions) {
        if (remainingGuests <= 0) {
            if (remainingGuests == 0 && !currentSuggestion.isEmpty()) {
                suggestions.add(new ArrayList<>(currentSuggestion));
            }
            return;
        }

        if (index >= roomTypes.size()) {
            return;
        }

        RoomType currentType = roomTypes.get(index);
        int roomCapacity = getCapacity(currentType);
        int maxRooms = Math.min(roomsByType.getOrDefault(currentType, new ArrayList<>()).size(), remainingGuests / roomCapacity);

        for (int count = maxRooms; count >= 0; count--) {
            int newRemaining = remainingGuests - (count * roomCapacity);
            if (count > 0) {
                currentSuggestion.add(new Pair<>(currentType, count));
            }
            generateSuggestions(roomTypes, index + 1, newRemaining, currentSuggestion, suggestions);

            if (count > 0) {
                currentSuggestion.removeLast();
            }
        }
    }

    @FXML
    private void handleSuggestionDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selectedSuggestion = suggestionsListView.getSelectionModel().getSelectedItem();

            if (selectedSuggestion != null) {
                parseSuggestionAndSetFields(selectedSuggestion);
                logger.log(Level.INFO, "Guest user selected a room suggestion.");
            }
        }
    }

    private void parseSuggestionAndSetFields(String suggestion) {
        singleRoomCount.setText("0");
        doubleRoomCount.setText("0");
        pentHouseCount.setText("0");
        deluxeSuiteCount.setText("0");

        String[] roomCounts = suggestion.split(", ");
        for (String roomCount : roomCounts) {
            String[] parts = roomCount.split(" x");
            if (parts.length == 2) {
                RoomType roomType = RoomType.valueOf(parts[0]);
                int count = Integer.parseInt(parts[1]);

                switch (roomType) {
                    case SINGLE -> singleRoomCount.setText(String.valueOf(count));
                    case DOUBLE -> doubleRoomCount.setText(String.valueOf(count));
                    case PENTHOUSE -> pentHouseCount.setText(String.valueOf(count));
                    case DELUXE -> deluxeSuiteCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private int getCapacity(RoomType roomType) {
        return roomTypeData.get(roomType).getKey();
    }
}
