package com.example.mhike.model;


public class Observation {
    public long id;
    public long hikeId;
    public String note;     // required
    public String datetime; // datetime string (yyyy-MM-dd HH:mm:ss)
    public String comments; // optional

    public Observation(long id, long hikeId, String note, String datetime, String comments) {
        this.id = id;
        this.hikeId = hikeId;
        this.note = note;
        this.datetime = datetime;
        this.comments = comments;
    }

    public Observation(long hikeId, String note, String datetime, String comments) {
        this(0, hikeId, note, datetime, comments);
    }
}