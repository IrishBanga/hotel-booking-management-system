package com.ib.hotelBookingManagementApplication.model;

import com.ib.hotelBookingManagementApplication.utilities.RoomType;

public class Room {
    private final int roomID;
    private final RoomType roomType;
    private final int numberOfBeds;
    private final float price;

    public Room(int roomID, RoomType roomType, int numberOfBeds, float price) {
        this.roomID = roomID;
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.price = price;
    }

    public int getRoomID() {
        return roomID;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public int getNumberOfBeds() {
        return numberOfBeds;
    }

    public float getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Room ID: " + roomID + ", Room Type: " + roomType + ", Number of Beds: " +
                numberOfBeds + ", Price: " + price;
    }
}
