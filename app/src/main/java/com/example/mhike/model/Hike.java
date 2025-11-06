package com.example.mhike.model;

public class Hike {
    public long id;
    public String name, location, date, description, photoUri; // ⬅️
    public boolean parking;
    public double lengthKm;
    public int difficulty;

    public Hike(long id, String name, String location, String date, boolean parking,
                double lengthKm, int difficulty, String description, String photoUri) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.date = date;
        this.parking = parking;
        this.lengthKm = lengthKm;
        this.difficulty = difficulty;
        this.description = description;
        this.photoUri = photoUri;
    }

    public Hike(String name, String location, String date, boolean parking,
                double lengthKm, int difficulty, String description, String photoUri) {
        this(0, name, location, date, parking, lengthKm, difficulty, description, photoUri);
    }
}