package com.ib.hotelBookingManagementApplication.utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class Helper {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public static String formatCurrency(double amount) {
        return CURRENCY.format(amount);
    }

    public static Double parseCurrency(String amount) {
        try {
            return CURRENCY.parse(amount).doubleValue();
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatDate(Date date) {
        return Helper.DATE_FORMAT.format(date);
    }

    public static void showAlert(Alert.AlertType alertType, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType.toString());
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setPrefSize(400, 300);
        alert.showAndWait();
    }

    public static void showAlertWithCallback(Alert.AlertType alertType, String header,
                                             String content, Consumer<Boolean> callback) {
        Alert alert = new Alert(alertType);
        alert.setTitle(String.valueOf(alertType));
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().setPrefSize(400, 300);
        alert.showAndWait();
        callback.accept(alert.getResult() == ButtonType.OK);
    }

    public static String getProjectPath() {
        return System.getProperty("user.dir");
    }

    public static void numericIntegerInputValidation(KeyEvent event) {
        TextField field = (TextField) event.getSource();
        try {
            // try to parse the input to an integer
            Integer.parseInt(field.getText());
        } catch (NumberFormatException e) {
            String updatedText = field.getText().replaceAll("\\D", "");
            field.setText(updatedText);
            field.positionCaret(updatedText.length());
        }
    }

    public static void numericDoubleInputValidation(KeyEvent event) {
        TextField field = (TextField) event.getSource();
        try {
            // try to parse the input to a double
            Double.parseDouble(field.getText());
        } catch (NumberFormatException e) {
            String updatedText = field.getText().replaceAll("[^\\d.]", "");
            boolean twoDots = updatedText.chars().filter(c -> c == '.').count() > 1;
            if (twoDots) {
                updatedText = updatedText.substring(0, updatedText.lastIndexOf('.'));
            }
            field.setText(updatedText);
            field.positionCaret(updatedText.length());
        }
    }

    public static void phoneInputValidation(KeyEvent event) {
        TextField field = (TextField) event.getSource();
        // perform phone number validation
        if (!field.getText().matches("\\d{0,10}")) {
            String phone = field.getText().replaceAll("\\D", "");
            if (phone.length() > 10)
                phone = phone.substring(0, 10);
            field.setText(phone);
            field.positionCaret(phone.length());
        }
    }


    public static void stringInputValidation(KeyEvent event) {
        TextField field = (TextField) event.getSource();
        // perform string validation allowing only alphanumeric characters and spaces
        if (!field.getText().matches("[\\da-zA-Z ]*")) {
            field.setText(field.getText().replaceAll("[^\\da-zA-Z ]", ""));
            field.positionCaret(field.getText().length());
        }
    }

    public static void textAreaInputValidation(KeyEvent event) {
        TextArea field = (TextArea) event.getSource();
        // perform string validation allowing only alphanumeric characters and spaces
        if (!field.getText().matches("[\\da-zA-Z .\\s]*")) {
            field.setText(field.getText().replaceAll("[^\\da-zA-Z .\\s]", ""));
            field.positionCaret(field.getText().length());
        }
    }
}
