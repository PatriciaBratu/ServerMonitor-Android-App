package com.example.servermonitor;

import android.content.ContentValues;
import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "server_logs.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DB_PASSWORD = "ParolaSecurizata123!";
    public static final String TABLE_LOGS = "logs";
    public static final String COL_ID = "id";
    public static final String COL_DATA = "data_ora";
    public static final String COL_SERVER = "nume_server";
    public static final String COL_STARE = "stare";
//    public static final String COL_DETALII = "detalii";
    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_LOGS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATA + " TEXT, " +
                COL_SERVER + " TEXT, " +
                COL_STARE + " TEXT )";
        db.execSQL(createTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int j) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        onCreate(db);
    }
    public void insereazaLog(String data, String server, String stare) {
        SQLiteDatabase db = this.getWritableDatabase(DB_PASSWORD);

        ContentValues values = new ContentValues();
        values.put(COL_DATA, data);
        values.put(COL_SERVER, server);
        values.put(COL_STARE, stare);

        db.insert(TABLE_LOGS, null, values);
        db.close();
    }
    public void stergeDatabase() {
        SQLiteDatabase db = this.getWritableDatabase(DB_PASSWORD);
        db.execSQL("DELETE FROM " + TABLE_LOGS);

        db.close();
    }
}
