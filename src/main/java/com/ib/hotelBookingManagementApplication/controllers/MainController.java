package com.ib.hotelBookingManagementApplication.controllers;

import com.ib.hotelBookingManagementApplication.utilities.Helper;
import com.ib.hotelBookingManagementApplication.utilities.LoggerUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;


public class MainController {
    @FXML
    private Menu adminLoginMenu;

    @FXML
    private MenuItem adminLoginMenuItem;

    @FXML
    private Button giveFeedbackButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button makeReservationButton;

    private Stage stage;
    private Scene scene;

    Logger logger = LoggerUtil.getLogger();

    @FXML
    void initialize() {

        makeReservationButton.setOnAction(event -> {
            try {
                setStageScene();

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ib/hotelBookingManagementApplication/main-reservation-view.fxml"));
                Scene currentScene = this.scene;
                Scene scene = new Scene(fxmlLoader.load());
                stage.setScene(scene);

                ReservationController controller = fxmlLoader.getController();
                controller.init(
                        val -> {
                            if (val) {
                                stage.setScene(currentScene);
                            }
                        }
                );

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load reservation screen", e);
            }

        });

        adminLoginMenuItem.setOnAction(event -> {
            Helper.showAlertWithCallback(Alert.AlertType.CONFIRMATION, "Welcome to IB Resorts",
                    """
                            Please note that this page is for admin use only.
                            If you are an admin, click "OK" to continue and log in.
                            If you are a guest, please click "Cancel" to return to go to reservation page.
                            """,
                    (response) -> {
                        if (response) {
                            try {
                                setStageScene();

                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ib/hotelBookingManagementApplication/admin-page.fxml"));
                                Scene currentScene = this.scene;
                                Scene scene = new Scene(fxmlLoader.load());
                                stage.setScene(scene);

                                AdminController controller = fxmlLoader.getController();
                                controller.init(
                                        val -> {
                                            if (val) {
                                                stage.setScene(currentScene);
                                            }
                                        }
                                );

                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Failed to load admin page: " + e.getMessage(), e);
                            }
                        } else {
                            makeReservationButton.fire();
                        }
                    }
            );
        });

        giveFeedbackButton.setOnAction(event -> {
            try {
                setStageScene();

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ib/hotelBookingManagementApplication/feedback-form-view.fxml"));
                Scene currentScene = this.scene;
                Scene scene = new Scene(fxmlLoader.load());
                stage.setScene(scene);

                FeedbackController controller = fxmlLoader.getController();
                controller.init(
                        val -> {
                            if (val) {
                                stage.setScene(currentScene);
                            }
                        }
                );

            } catch (Exception e) {
                logger.log(Level.INFO, "Failed to load feedback form view: " + e.getMessage(), e);
            }
        });

        helpButton.setOnAction(event -> {
            setStageScene();
            HelpAndInformationController.loadHelpScreen(stage, scene);
        });
    }

    private void setStageScene() {
        stage = (Stage) makeReservationButton.getScene().getWindow();
        scene = makeReservationButton.getScene();
    }

}
