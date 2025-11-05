package com.example.mhike.model;

public class Hike {
    public String name;
    public String location;
    public String date; // UI-only for now
    public boolean parking;
    public double lengthKm;
    public int difficulty; // 1..5
    public String description;

    public Hike(String name, String location, String date, boolean parking,
                double lengthKm, int difficulty, String description) {
        this.name = name;
        this.location = location;
        this.date = date;
        this.parking = parking;
        this.lengthKm = lengthKm;
        this.difficulty = difficulty;
        this.description = description;
    }
}