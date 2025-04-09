module com.ib.hotelBookingManagementApplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.xerial.sqlitejdbc;

    opens com.ib.hotelBookingManagementApplication to javafx.fxml;
    exports com.ib.hotelBookingManagementApplication;
    exports com.ib.hotelBookingManagementApplication.controllers;
    opens com.ib.hotelBookingManagementApplication.controllers to javafx.fxml;
}
