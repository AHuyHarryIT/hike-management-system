package com.example.mhike.model;


public class Observation {
    public long id;
    public long hikeId;
    public String note;     // required
    public long timeSec;    // epoch seconds
    public String comments; // optional

    public Observation(long id, long hikeId, String note, long timeSec, String comments) {
        this.id = id;
        this.hikeId = hikeId;
        this.note = note;
        this.timeSec = timeSec;
        this.comments = comments;
    }

    public Observation(long hikeId, String note, long timeSec, String comments) {
        this(0, hikeId, note, timeSec, comments);
    }
}