package com.ib.hotelBookingManagementApplication.model;

public class Guest {
    private int guestID;
    private String name;
    private String phoneNumber;
    private String email;

    public Guest(String name, String phoneNumber, String email) {
        this.guestID = -1;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public Guest(int guestID, String name, String phoneNumber, String email) {
        this.guestID = guestID;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public int getGuestID() {
        return guestID;
    }

    public void setGuestID(int guestID) {
        this.guestID = guestID;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String newEmail) {
        email = newEmail;
    }

    public void setPhone(String newPhoneNumber) {
        phoneNumber = newPhoneNumber;
    }

    @Override
    public String toString() {
        return "GUEST ID: " + guestID + "\nNAME: " + name + "\nPHONE NUMBER: " + phoneNumber + "\nEMAIL: " + email;
    }
}
