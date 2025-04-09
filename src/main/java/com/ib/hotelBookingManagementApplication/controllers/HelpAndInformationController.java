package com.ib.hotelBookingManagementApplication.controllers;

import com.ib.hotelBookingManagementApplication.utilities.LoggerUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.function.Consumer;
import java.util.logging.Level;

public class HelpAndInformationController {
    @FXML
    private Button closeButton;

    @FXML
    private TextArea helpTextArea;

    @FXML
    public void initialize() {
        helpTextArea.setText("""
                Welcome to the Help and Information section!
                
                Here, you can find answers to common questions and issues you may encounter while using the kiosk application.
                1. How to make a reservation?
                - To make a reservation, navigate to the 'Make a Reservation' button on home screen.
                - You will be prompted to enter your check-in and check-out dates, number of guests.
                - The application will find available rooms for your selected dates and make suggestions.
                - You can either select a room from the suggestions or enter your own preferences.
                - Once you have selected the room(s), you will be prompted to enter your personal details.
                - After entering your details, you will be shown a summary of your reservation.
                - Review the details and click on the 'Confirm' button to finalize your reservation.
                
                PLEASE NOTE: For check-out and payment, please contact the hotel directly.
                
                2. How to change a reservation?
                - To change a reservation, please contact the hotel directly.
                - You may need to provide your reservation ID and guest ID for verification.
                
                3. How to cancel a reservation?
                - Please contact the hotel directly to cancel your reservation.
                - You may need to provide your reservation ID and guest ID for verification.
                - Cancellation policies may vary, so please check with the hotel for specific details.
                
                4. How to provide feedback?
                - To provide feedback after your stay, please navigate to the 'Feedback' button on the home screen.
                - Fill in the feedback form with your comments and rating.
                - Click on the 'Submit Feedback' button to send your feedback.
                
                4. How to contact support?
                - Please call 123-456-7890 to reach our customer support team.
                - You can also send an email to customercare@ibresorts.ca for assistance.
                """);
    }

    public static void loadHelpScreen(Stage stage, Scene currentScene) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelpAndInformationController.class.getResource("/com/ib/hotelBookingManagementApplication/information-et-help-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);

            HelpAndInformationController controller = fxmlLoader.getController();
            controller.init(
                    val -> {
                        if (val) {
                            stage.setScene(currentScene);
                        }
                    }
            );
        } catch (Exception e) {
            LoggerUtil.getLogger().log(Level.SEVERE, "Failed to load help screen", e);
        }
    }

    public void init(Consumer<Boolean> onCancellation) {
        closeButton.setOnAction(event -> onCancellation.accept(true));
    }
}
