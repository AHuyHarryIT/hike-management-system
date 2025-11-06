package com.example.mhike.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mhike.model.Observation;

import java.util.ArrayList;
import java.util.List;

public class ObservationDao {
    private final DbHelper helper;

    public ObservationDao(Context ctx) {
        this.helper = new DbHelper(ctx.getApplicationContext());
    }

    public long insert(Observation o) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("hike_id", o.hikeId);
        v.put("note", o.note);
        v.put("time_sec", o.timeSec);
        v.put("comments", o.comments);
        return db.insertOrThrow("observations", null, v);
    }

    public int update(Observation o) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("note", o.note);
        v.put("comments", o.comments);
        // keep time_sec as the original event time
        return db.update("observations", v, "id=?", new String[]{String.valueOf(o.id)});
    }

    public int delete(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("observations", "id=?", new String[]{String.valueOf(id)});
    }

    public Observation findById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query("observations",
                new String[]{"id", "hike_id", "note", "time_sec", "comments"},
                "id=?", new String[]{String.valueOf(id)}, null, null, null, "1");
        try {
            if (c.moveToFirst()) {
                return new Observation(
                        c.getLong(0), c.getLong(1), c.getString(2), c.getLong(3), c.getString(4)
                );
            }
            return null;
        } finally {
            c.close();
        }
    }

    public List<Observation> listByHike(long hikeId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query("observations",
                new String[]{"id", "hike_id", "note", "time_sec", "comments"},
                "hike_id=?", new String[]{String.valueOf(hikeId)},
                null, null, "time_sec DESC, id DESC");
        List<Observation> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                list.add(new Observation(
                        c.getLong(0), c.getLong(1), c.getString(2), c.getLong(3), c.getString(4)
                ));
            }
        } finally {
            c.close();
        }
        return list;
    }
}