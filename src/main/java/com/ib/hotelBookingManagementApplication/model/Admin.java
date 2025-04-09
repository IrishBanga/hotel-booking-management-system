package com.ib.hotelBookingManagementApplication.model;

import com.ib.hotelBookingManagementApplication.database.DatabaseUtil;

public class Admin {
    private final String username;
    private final String password;
    private int adminID;

    public Admin(String username, String password) {
        this.adminID = 0;
        this.username = username;
        this.password = password;
    }

    public int getAdminID() {
        return adminID;
    }

    public String getUsername() {
        return username;
    }

    public boolean login() {
        adminID = DatabaseUtil.validateLogin(username, password);
        return adminID > 0;
    }
}
