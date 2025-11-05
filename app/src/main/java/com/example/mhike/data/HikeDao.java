package com.example.mhike.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mhike.model.Hike;

import java.util.ArrayList;
import java.util.List;

public class HikeDao {

    private final DbHelper helper;

    public HikeDao(Context ctx) {
        this.helper = new DbHelper(ctx.getApplicationContext());
    }

    public long insert(Hike h) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", h.name);
        v.put("location", h.location);
        v.put("date", h.date);
        v.put("parking", h.parking ? 1 : 0);
        v.put("length_km", h.lengthKm);
        v.put("difficulty", h.difficulty);
        v.put("description", h.description);
        return db.insertOrThrow("hikes", null, v);
    }

    public int update(Hike h) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", h.name);
        v.put("location", h.location);
        v.put("date", h.date);
        v.put("parking", h.parking ? 1 : 0);
        v.put("length_km", h.lengthKm);
        v.put("difficulty", h.difficulty);
        v.put("description", h.description);
        return db.update("hikes", v, "id=?", new String[]{String.valueOf(h.id)});
    }

    public int delete(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("hikes", "id=?", new String[]{String.valueOf(id)});
    }

    public List<Hike> getAll(String queryLike) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> args = new ArrayList<>();
        String where = null;
        if (queryLike != null && !queryLike.trim().isEmpty()) {
            where = "(name LIKE ? OR location LIKE ?)";
            String like = "%" + queryLike.trim() + "%";
            args.add(like);
            args.add(like);
        }

        Cursor c = db.query(
                "hikes",
                new String[]{"id", "name", "location", "date", "parking", "length_km", "difficulty", "description"},
                where,
                args.isEmpty() ? null : args.toArray(new String[0]),
                null, null,
                "date DESC, id DESC"
        );
        List<Hike> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                String name = c.getString(1);
                String location = c.getString(2);
                String date = c.getString(3);
                boolean parking = c.getInt(4) == 1;
                double lengthKm = c.getDouble(5);
                int difficulty = c.getInt(6);
                String description = c.getString(7);
                list.add(new Hike(id, name, location, date, parking, lengthKm, difficulty, description));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public Hike findById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query("hikes",
                new String[]{"id", "name", "location", "date", "parking", "length_km", "difficulty", "description"},
                "id=?", new String[]{String.valueOf(id)}, null, null, null, "1");
        try {
            if (c.moveToFirst()) {
                return new Hike(
                        c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
                        c.getInt(4) == 1, c.getDouble(5), c.getInt(6), c.getString(7)
                );
            }
            return null;
        } finally {
            c.close();
        }
    }
}