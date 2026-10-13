package com.travelsync.model;

public class Hotel {
    private final String name;
    private final String city;
    private final String description;
    private final int pricePerNight;
    private int roomsAvailable;

    public Hotel(String name, String city, String description, int pricePerNight, int roomsAvailable) {
        this.name = name;
        this.city = city;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.roomsAvailable = roomsAvailable;
    }

    public String getName() { return name; }
    public String getCity() { return city; }
    public String getDescription() { return description; }
    public int getPricePerNight() { return pricePerNight; }
    public int getRoomsAvailable() { return roomsAvailable; }

    public boolean hasRoom() { return roomsAvailable > 0; }

    // reserves one room; false if sold out
    public boolean takeRoom() {
        if (roomsAvailable <= 0) return false;
        roomsAvailable--;
        return true;
    }

    public void releaseRoom() { roomsAvailable++; }

    @Override
    public String toString() {
        return name + " [" + city + "]";
    }
}