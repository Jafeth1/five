package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Inventory.db";
    private static final int DATABASE_VERSION = 16; // Adjusted version
    private static InventoryDbHelper instance;
    private static final String CREATE_COG_VALUES_TABLE = "CREATE TABLE IF NOT EXISTS cog_values (" +
            "item TEXT PRIMARY KEY," +
            "cog_value REAL" +
            ");";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS inventory (item TEXT PRIMARY KEY, quantity REAL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS forecast_table (id INTEGER PRIMARY KEY AUTOINCREMENT, forecast REAL NOT NULL)");
        db.execSQL(CREATE_COG_VALUES_TABLE);
    }

    public static synchronized InventoryDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new InventoryDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS inventory");
        db.execSQL("DROP TABLE IF EXISTS forecast_table");
        db.execSQL("DROP TABLE IF EXISTS editTextValues");
        onCreate(db);
    }
}