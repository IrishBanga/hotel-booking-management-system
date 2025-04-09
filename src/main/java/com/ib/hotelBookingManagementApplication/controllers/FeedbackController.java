package com.ib.hotelBookingManagementApplication.controllers;

import com.ib.hotelBookingManagementApplication.database.DatabaseUtil;
import com.ib.hotelBookingManagementApplication.model.Feedback;
import com.ib.hotelBookingManagementApplication.utilities.Helper;
import com.ib.hotelBookingManagementApplication.utilities.LoggerUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.function.Consumer;
import java.util.logging.Level;

public class FeedbackController {
    @FXML
    private TextField bookingIDFeedbackFormTextField;
    @FXML
    private Button closeButton;
    @FXML
    private TextArea commentsFeedbackFormField;
    @FXML
    private TextField guestIDFeedbackFormTextField;
    @FXML
    private Slider ratingFeedbackFormSlider;
    @FXML
    private Button submitFeedbackButton;

    public void initialize() {
        submitFeedbackButton.setOnAction(event -> {
            submitFeedback();
        });

        guestIDFeedbackFormTextField.setOnKeyReleased(Helper::numericIntegerInputValidation);
        bookingIDFeedbackFormTextField.setOnKeyReleased(Helper::numericIntegerInputValidation);
        commentsFeedbackFormField.setOnKeyReleased(Helper::textAreaInputValidation);

        LoggerUtil.getLogger().log(Level.INFO, "FeedbackController initialized");
    }

    private void submitFeedback() {
        try {
            int guestID = Integer.parseInt(guestIDFeedbackFormTextField.getText());
            int reservationID = Integer.parseInt(bookingIDFeedbackFormTextField.getText());
            String comments = commentsFeedbackFormField.getText().trim();
            int rating = (int) ratingFeedbackFormSlider.getValue();

            if (comments.isEmpty()) {
                Helper.showAlert(Alert.AlertType.ERROR, "Input Error", "Comments cannot be empty.");
                return;
            }
            if (rating < 1 || rating > 5) {
                Helper.showAlert(Alert.AlertType.ERROR, "Input Error", "Rating must be between 1 and 5.");
                return;
            }
            if (!DatabaseUtil.checkReservationInformation(reservationID, guestID)) {
                Helper.showAlert(Alert.AlertType.ERROR, "Input Error", """
                        Provided guest and booking IDs do not match any checked-out reservation.
                        Please ensure that you have checked out of the reservation before submitting feedback!
                        Please check your input values and try again...""");
                return;
            }
            if (DatabaseUtil.checkFeedbackExists(reservationID, guestID)) {
                Helper.showAlert(Alert.AlertType.ERROR, "Input Error",
                        "Feedback already submitted for this reservation.");
                return;
            }

            Feedback feedback = new Feedback(guestID, reservationID, comments, rating);
            if (DatabaseUtil.submitFeedback(feedback)) {
                Helper.showAlert(Alert.AlertType.INFORMATION, "Feedback Submitted",
                        "Thank you for your feedback! We appreciate your input.");
                closeButton.fire();
            } else {
                Helper.showAlert(Alert.AlertType.ERROR, "Submission Error",
                        "An error occurred while submitting your feedback. Please try again.");
            }

        } catch (NumberFormatException e) {
            Helper.showAlert(Alert.AlertType.ERROR, "Input Error",
                    "Please enter valid numbers for Guest ID and Reservation ID.");

            LoggerUtil.getLogger().log(Level.SEVERE, "Input Error", e.getMessage());

        } catch (Exception e) {
            Helper.showAlert(Alert.AlertType.ERROR, "Submission Error",
                    "An error occurred while submitting your feedback. Please try again.");

            LoggerUtil.getLogger().log(Level.SEVERE, "Feedback Submission Error", e.getMessage());
        }
    }

    public void init(Consumer<Boolean> onCancellation) {
        closeButton.setOnAction(event -> onCancellation.accept(true));
    }
}
