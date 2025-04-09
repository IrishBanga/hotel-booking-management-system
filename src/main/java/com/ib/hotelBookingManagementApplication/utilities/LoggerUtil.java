package com.ib.hotelBookingManagementApplication.utilities;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {
    public static Logger logger = Logger.getLogger("IBResortsBookingSystemLogger");

    static {
        try {
            FileHandler fileHandler = new FileHandler("ib-resorts-booking-system-logs.%g.log", 1024 * 1024, 10,
                    true);
            logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize logger", e);
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
