package com.ib.hotelBookingManagementApplication.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reservation {
    private final String status;
    private int reservationID;
    private int guestID;
    private List<Integer> roomsList;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private int numberOfGuests;

    public Reservation(int reservationID, int guestID, LocalDate checkinDate, LocalDate checkoutDate,
                       int numberOfGuests, String status) {
        this.reservationID = reservationID;
        this.guestID = guestID;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
        this.roomsList = new ArrayList<>();
        this.numberOfGuests = numberOfGuests;
        this.status = status;
    }

    public Reservation() {
        this.reservationID = -1;
        this.guestID = -1;
        this.checkinDate = null;
        this.checkoutDate = null;
        this.numberOfGuests = -1;
        this.status = "Pending";
    }

    public int getReservationID() {
        return reservationID;
    }

    public void setReservationID(int reservationID) {
        this.reservationID = reservationID;
    }

    public int getGuestID() {
        return guestID;
    }

    public void setGuestID(int guestID) {
        this.guestID = guestID;
    }

    public List<Integer> getRoomsList() {
        return roomsList;
    }

    public void setRoomsList(List<Integer> roomsList) {
        this.roomsList = roomsList;
    }

    public LocalDate getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(LocalDate checkinDate) {
        this.checkinDate = checkinDate;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(LocalDate checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    @Override
    public String toString() {
        return "RESERVATION ID: " + reservationID + "\nGUEST ID: " + guestID + "\nROOM(s): " + roomsList +
                "\nCHECK-IN DATE: " + checkinDate + "\nCHECK-OUT DATE: " + checkoutDate + "\nNUMBER OF GUESTS: "
                + numberOfGuests + "\nSTATUS: " + status;
    }
}
