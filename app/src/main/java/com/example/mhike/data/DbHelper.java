package com.example.mhike.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "mhike.db";
    public static final int DB_VERSION = 3;

    public DbHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE hikes (" +
                        "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  name TEXT NOT NULL," +
                        "  location TEXT NOT NULL," +
                        "  date TEXT NOT NULL," +
                        "  parking INTEGER NOT NULL," +
                        "  length_km REAL NOT NULL," +
                        "  difficulty INTEGER NOT NULL," +
                        "  description TEXT," +
                        "  photo_uri TEXT," +
                        "  created_at INTEGER DEFAULT (strftime('%s','now'))" +
                        ");"
        );
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hikes_name ON hikes(name);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hikes_location ON hikes(location);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hikes_date ON hikes(date);");

        createObservations(db);
    }

    private void createObservations(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS observations (" +
                        "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  hike_id INTEGER NOT NULL," +
                        "  note TEXT NOT NULL," +
                        "  time_sec INTEGER NOT NULL," +
                        "  comments TEXT," +
                        "  FOREIGN KEY(hike_id) REFERENCES hikes(id) ON DELETE CASCADE" +
                        ");"
        );
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_obs_hike ON observations(hike_id);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_obs_time ON observations(time_sec);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createObservations(db);
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE hikes ADD COLUMN photo_uri TEXT;");
        }
    }
}