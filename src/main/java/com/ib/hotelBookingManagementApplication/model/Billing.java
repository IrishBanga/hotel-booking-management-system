package com.ib.hotelBookingManagementApplication.model;

public class Billing {
    private final int reservationID;
    private double amount;
    private final double taxRate = 0.13;
    private double taxAmount;
    private double discount;

    private double totalAmount;

    public Billing(int reservationID, double amount, double discount) {
        this.reservationID = reservationID;
        this.amount = amount;
        this.discount = discount;
        calculateTotalAmount();
    }

    public int getReservationID() {
        return reservationID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        calculateTotalAmount();
    }

    public double getTax() {
        return taxAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        calculateTotalAmount();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    private void calculateTotalAmount() {
        double discountOnAmount = amount * discount / 100;
        double discountedAmount = amount - discountOnAmount;
        double tax = discountedAmount * taxRate;
        this.taxAmount = tax;
        totalAmount = discountedAmount + tax;
    }

}
