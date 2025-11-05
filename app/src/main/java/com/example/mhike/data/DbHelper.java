package com.example.mhike.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "mhike.db";
    public static final int DB_VERSION = 1;

    public DbHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE hikes (" +
                        "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  name TEXT NOT NULL," +
                        "  location TEXT NOT NULL," +
                        "  date TEXT NOT NULL," +              // YYYY-MM-DD
                        "  parking INTEGER NOT NULL," +        // 0/1
                        "  length_km REAL NOT NULL," +
                        "  difficulty INTEGER NOT NULL," +     // 1..5
                        "  description TEXT," +
                        "  created_at INTEGER DEFAULT (strftime('%s','now'))" +
                        ");"
        );
        // Optional index examples:
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hikes_name ON hikes(name);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hikes_location ON hikes(location);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_hikes_date ON hikes(date);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS hikes;");
        onCreate(db);
    }
}
