package com.example.mhike.model;

public class Hike {
    public long id;
    public String name;
    public String location;
    public String date;
    public boolean parking;
    public double lengthKm;
    public int difficulty;
    public String description;

    public Hike(long id, String name, String location, String date, boolean parking,
                double lengthKm, int difficulty, String description) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.date = date;
        this.parking = parking;
        this.lengthKm = lengthKm;
        this.difficulty = difficulty;
        this.description = description;
    }

    public Hike(String name, String location, String date, boolean parking,
                double lengthKm, int difficulty, String description) {
        this(0, name, location, date, parking, lengthKm, difficulty, description);
    }
}