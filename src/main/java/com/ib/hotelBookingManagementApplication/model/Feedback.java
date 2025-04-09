package com.ib.hotelBookingManagementApplication.model;

public class Feedback {
    private final int guestID;
    private final int reservationID;
    private final String comments;
    private final int rating;

    public Feedback(int guestID, int reservationID, String comments, int rating) {
        this.guestID = guestID;
        this.reservationID = reservationID;
        this.comments = comments;
        this.rating = rating;
    }

    public int getGuestID() {
        return guestID;
    }

    public int getReservationID() {
        return reservationID;
    }

    public String getComments() {
        return comments;
    }

    public int getRating() {
        return rating;
    }
}
