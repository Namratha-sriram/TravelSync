package com.travelsync.model;

public class Booking {
    private final String code;
    private final String customer;
    private final String destination;
    private final String hotel;
    private final int nights;
    private final int amount;
    private final String bookedAt;

    public Booking(String code, String customer, String destination, String hotel,
                   int nights, int amount, String bookedAt) {
        this.code = code;
        this.customer = customer;
        this.destination = destination;
        this.hotel = hotel;
        this.nights = nights;
        this.amount = amount;
        this.bookedAt = bookedAt;
    }

    public String getCode() { return code; }
    public String getCustomer() { return customer; }
    public String getDestination() { return destination; }
    public String getHotel() { return hotel; }
    public int getNights() { return nights; }
    public int getAmount() { return amount; }
    public String getBookedAt() { return bookedAt; }

    @Override
    public String toString() {
        return code + " | " + customer + " | " + destination + " | " + hotel
                + " | " + nights + " night(s) | Rs " + amount + " | " + bookedAt;
    }
}