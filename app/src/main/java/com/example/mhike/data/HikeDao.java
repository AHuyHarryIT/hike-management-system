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
        v.put("photo_uri", h.photoUri);
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
        v.put("photo_uri", h.photoUri);
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
                new String[]{"id", "name", "location", "date", "parking", "length_km", "difficulty", "description", "photo_uri"},
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
                String photoUri = c.getString(8);
                list.add(new Hike(id, name, location, date, parking, lengthKm, difficulty, description, photoUri));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public Hike findById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(
                "hikes",
                new String[]{"id", "name", "location", "date", "parking", "length_km", "difficulty", "description", "photo_uri"},
                "id=?",
                new String[]{String.valueOf(id)},
                null, null, null, "1"
        );
        try {
            if (c.moveToFirst()) {
                return new Hike(
                        c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
                        c.getInt(4) == 1, c.getDouble(5), c.getInt(6),
                        c.getString(7), c.getString(8)
                );
            }
            return null;
        } finally {
            c.close();
        }
    }

    public List<Hike> getFiltered(String name, String location,
                                  String dateFrom, String dateTo,
                                  Double lenMin, Double lenMax,
                                  Integer diffMin, Integer diffMax,
                                  Integer parking /* null=any, 1=yes, 0=no */) {
        SQLiteDatabase db = helper.getReadableDatabase();

        StringBuilder where = new StringBuilder();
        List<String> args = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            append(where, "name LIKE ?");
            args.add("%" + name + "%");
        }
        if (location != null && !location.isEmpty()) {
            append(where, "location LIKE ?");
            args.add("%" + location + "%");
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            append(where, "date >= ?");
            args.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            append(where, "date <= ?");
            args.add(dateTo);
        }
        if (lenMin != null) {
            append(where, "length_km >= ?");
            args.add(String.valueOf(lenMin));
        }
        if (lenMax != null) {
            append(where, "length_km <= ?");
            args.add(String.valueOf(lenMax));
        }
        if (diffMin != null) {
            append(where, "difficulty >= ?");
            args.add(String.valueOf(diffMin));
        }
        if (diffMax != null) {
            append(where, "difficulty <= ?");
            args.add(String.valueOf(diffMax));
        }
        if (parking != null) {
            append(where, "parking = ?");
            args.add(String.valueOf(parking));
        }

        Cursor c = db.query(
                "hikes",
                new String[]{"id", "name", "location", "date", "parking", "length_km", "difficulty", "description", "photo_uri"},
                where.length() == 0 ? null : where.toString(),
                args.isEmpty() ? null : args.toArray(new String[0]),
                null, null,
                "date DESC, id DESC"
        );

        List<Hike> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                String name_ = c.getString(1);
                String loc_ = c.getString(2);
                String date_ = c.getString(3);
                boolean parking_ = c.getInt(4) == 1;
                double lengthKm = c.getDouble(5);
                int diff = c.getInt(6);
                String desc = c.getString(7);
                String photo = c.getString(8);
                list.add(new Hike(id, name_, loc_, date_, parking_, lengthKm, diff, desc, photo));
            }
        } finally {
            c.close();
        }
        return list;
    }

    private void append(StringBuilder sb, String clause) {
        if (sb.length() > 0) sb.append(" AND ");
        sb.append(clause);
    }
}