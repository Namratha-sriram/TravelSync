package com.travelsync.model;

import java.util.List;

public class Destination {
    private final String name;
    private final String country;
    private final String description;
    private final List<String> attractions;

    public Destination(String name, String country, String description, List<String> attractions) {
        this.name = name;
        this.country = country;
        this.description = description;
        this.attractions = attractions;
    }

    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getDescription() { return description; }
    public List<String> getAttractions() { return attractions; }

    @Override
    public String toString() {
        return name + " (" + country + ") - " + attractions;
    }
}